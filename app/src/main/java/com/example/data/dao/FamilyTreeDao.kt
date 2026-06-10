package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyTreeDao {
    // --- User operations ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE isSessionActive = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isSessionActive = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isSessionActive = 0")
    suspend fun clearAllSessions()

    // --- Tree operations ---
    @Query("SELECT * FROM trees WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTreesForUserFlow(userId: Int): Flow<List<TreeEntity>>

    @Query("SELECT * FROM trees WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getTreesForUser(userId: Int): List<TreeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTree(tree: TreeEntity): Long

    @Query("DELETE FROM trees WHERE id = :treeId")
    suspend fun deleteTree(treeId: Int)

    // --- Member operations ---
    @Query("SELECT * FROM members WHERE treeId = :treeId ORDER BY lastName, firstName")
    fun getMembersForTreeFlow(treeId: Int): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE treeId = :treeId ORDER BY lastName, firstName")
    suspend fun getMembersForTree(treeId: Int): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun getMemberFlow(memberId: Int): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: Int): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    // --- Relationship operations ---
    @Query("SELECT * FROM relationships WHERE treeId = :treeId")
    fun getRelationshipsForTreeFlow(treeId: Int): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE treeId = :treeId")
    suspend fun getRelationshipsForTree(treeId: Int): List<RelationshipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: RelationshipEntity): Long

    @Delete
    suspend fun deleteRelationship(relationship: RelationshipEntity)

    @Query("DELETE FROM relationships WHERE memberId = :memberId OR relatedMemberId = :memberId")
    suspend fun deleteRelationshipsForMember(memberId: Int)

    // --- Story operations ---
    @Query("SELECT * FROM stories WHERE treeId = :treeId ORDER BY dateOccurred DESC")
    fun getStoriesForTreeFlow(treeId: Int): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE memberId = :memberId ORDER BY dateOccurred DESC")
    fun getStoriesForMemberFlow(memberId: Int): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Delete
    suspend fun deleteStory(story: StoryEntity)

    // --- Document operations ---
    @Query("SELECT * FROM documents WHERE treeId = :treeId ORDER BY dateAdded DESC")
    fun getDocumentsForTreeFlow(treeId: Int): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE memberId = :memberId ORDER BY dateAdded DESC")
    fun getDocumentsForMemberFlow(memberId: Int): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
}
