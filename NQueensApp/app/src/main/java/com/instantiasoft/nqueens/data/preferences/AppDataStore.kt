package com.instantiasoft.nqueens.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.instantiasoft.nqueens.data.model.BestTimes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data_store")

@Module
@InstallIn(SingletonComponent::class)
object AppDataStoreModule {
    @Singleton
    @Provides
    fun provideAppDataStore(
        @ApplicationContext appContext: Context
    ): AppDataStore {
        return AppDataStore(appContext)
    }
}

@Singleton
class AppDataStore(
    private val context: Context
) {
    private object Keys {
        val BOARD_SIZE = intPreferencesKey("board_size")
        val SHOW_MOVES = booleanPreferencesKey("show_moves")
        val BEST_TIMES = stringPreferencesKey("best_times")
    }

    val boardSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.BOARD_SIZE] ?: 8
    }

    val showMoves: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_MOVES] ?: false
    }

    val bestTimes: Flow<BestTimes> = context.dataStore.data.map { prefs ->
        prefs[Keys.BEST_TIMES]?.let {
            try {
                Json.decodeFromString<BestTimes>(it)
            } catch (_: Exception) {
                null
            }
        } ?: BestTimes()
    }

    suspend fun setBoardSize(size: Int) {
        context.dataStore.edit { settings ->
            settings[Keys.BOARD_SIZE] = size
        }
    }

    suspend fun setShowMoves(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[Keys.SHOW_MOVES] = show
        }
    }

    suspend fun setBestTimes(times: BestTimes) {
        context.dataStore.edit { settings ->
            try {
                settings[Keys.BEST_TIMES] = Json.encodeToString(BestTimes.serializer(), times)
            } catch (_: Exception) {
            }
        }
    }
}