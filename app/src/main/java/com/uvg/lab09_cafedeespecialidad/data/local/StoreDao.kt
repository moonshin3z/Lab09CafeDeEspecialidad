package com.uvg.lab09_cafedeespecialidad.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import com.uvg.lab09_cafedeespecialidad.data.local.entity.FavoriteEntity
import com.uvg.lab09_cafedeespecialidad.data.local.entity.OrderLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Query(
        """
        SELECT *
        FROM favorites
        ORDER BY productId ASC
        """
    )
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query(
        """
        DELETE FROM favorites
        WHERE productId = :productId
        """
    )
    suspend fun deleteFavorite(productId: String)

    @Query(
        """
        SELECT *
        FROM order_lines
        ORDER BY productId ASC
        """
    )
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Upsert
    suspend fun upsertOrderLine(orderLine: OrderLineEntity)

    @Query(
        """
        DELETE FROM order_lines
        WHERE productId = :productId
        """
    )
    suspend fun deleteOrderLine(productId: String)

    @Query("DELETE FROM order_lines")
    suspend fun clearOrderLines()
}