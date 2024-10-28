package com.example.magnise.data.price

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.magnise.model.Currency
import java.math.BigDecimal

object PriceCache {

    private lateinit var db: PriceDatabase

    fun init(context: Context) {
        if (!this::db.isInitialized) {
            db = Room.databaseBuilder(
                context,
                PriceDatabase::class.java, "database-name"
            ).build()
        }
    }

    fun getInstruments(): List<com.example.magnise.model.Instrument> {
        return db.instrumentDao().getAll().map {
            com.example.magnise.model.Instrument(it.id, it.base, it.quote)
        }
    }

    fun updateInstruments(instruments: List<com.example.magnise.model.Instrument>) {
        db.instrumentDao().insertAll(*instruments.map {
                Instrument(it.id, it.base, it.quote)
            }.toTypedArray()
        )
    }

    fun getPriceHistory(instrument: com.example.magnise.model.Instrument): List<com.example.magnise.model.Price> {
        val output = db.priceHistoryDao().getByInstrumentId(instrument.id)
        return output.priceHistory.map {
            com.example.magnise.model.Price(instrument, it.value, it.timestamp)
        }
    }

    fun updatePriceHistory(history: List<com.example.magnise.model.Price>) {
        db.priceHistoryDao().insertAll(*history.map {
            PriceHistoryItem("${it.instrument.id}_${it.timestamp}", it.instrument.id, it.value, it.timestamp)
        }.toTypedArray())
    }

}

@Database(entities = [Instrument::class, PriceHistoryItem::class], version = 1)
@TypeConverters(PriceTypeConverter::class)
abstract class PriceDatabase : RoomDatabase() {

    abstract fun instrumentDao(): InstrumentDao

    abstract fun priceHistoryDao(): PriceHistoryDao

}

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instrument")
    fun getAll(): List<Instrument>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg instruments: Instrument)

}

@Entity(tableName = "instrument")
data class Instrument(
    @PrimaryKey val id: String,
    val base: Currency,
    val quote: Currency
)

@Dao
interface PriceHistoryDao {

    @Transaction
    @Query("SELECT * FROM instrument WHERE id = :instrumentId")
    fun getByInstrumentId(instrumentId: String): PriceHistory

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg priceHistoryItem: PriceHistoryItem)

}

data class PriceHistory(
    @Embedded val instrument: Instrument,
    @Relation(
        parentColumn = "id",
        entityColumn = "instrumentId",
        entity = PriceHistoryItem::class
    )
    val priceHistory: List<PriceHistoryItem>
)

@Entity(
    tableName = "price_history_items",
    foreignKeys = [
        ForeignKey(
            entity = Instrument::class,
            parentColumns = ["id"],
            childColumns = ["instrumentId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["instrumentId"])]
)
data class PriceHistoryItem(
    @PrimaryKey val id: String,
    val instrumentId: String,
    val value: BigDecimal,
    val timestamp: Long,
)

class PriceTypeConverter {

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal?): String {
        return input?.toPlainString() ?: ""
    }

    @TypeConverter
    fun stringToBigDecimal(input: String?): BigDecimal {
        if (input.isNullOrBlank()) return BigDecimal.valueOf(0.0)
        return input.toBigDecimalOrNull() ?: BigDecimal.valueOf(0.0)
    }

    @TypeConverter
    fun currencyToString(input: Currency?): String {
        return input?.id ?: ""
    }

    @TypeConverter
    fun stringToCurrency(input: String?): Currency {
        if (input.isNullOrBlank()) return Currency.getInstance("")
        return Currency.getInstance(input)
    }

}