package com.example.android.politicalpreparedness.database

import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    /**
     * Insert a election in the database.
     *
     * @param election the task to be inserted.
     */
    @Insert()
    suspend fun saveElection(election: Election)

    /**
     * Select all elections from the tasks table.
     *
     * @return all election saved.
     */
    @Query("SELECT * FROM election_table")
    suspend fun getElections(): List<Election>

    /**
     * Select a task by id.
     *
     * @param electionId the election id.
     * @return the election with electionId.
     */
    @Query("SELECT * FROM election_table WHERE id = :electionId")
    suspend fun getElectionById(electionId: Int): Election?

    /**
     * Delete a election by id.
     *
     * @return the number of tasks deleted. This should always be 1.
     */
    @Query("DELETE FROM election_table WHERE id = :electionId")
    suspend fun deleteElectionById(electionId: Int): Int

    /**
     * Delete all elections.
     */
    @Query("DELETE FROM election_table")
    suspend fun deleteElections()

}