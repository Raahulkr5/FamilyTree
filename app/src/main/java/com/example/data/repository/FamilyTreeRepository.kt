package com.example.data.repository

import com.example.data.dao.FamilyTreeDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class FamilyTreeRepository(private val dao: FamilyTreeDao) {
    // Users
    suspend fun getUserByEmail(email: String) = dao.getUserByEmail(email)
    suspend fun getUserByPhone(phone: String) = dao.getUserByPhone(phone)
    val activeUserFlow: Flow<UserEntity?> = dao.getActiveUserFlow()
    suspend fun getActiveUser() = dao.getActiveUser()
    suspend fun registerUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    suspend fun logoutAll() = dao.clearAllSessions()

    // Trees
    fun getTreesForUserFlow(userId: Int) = dao.getTreesForUserFlow(userId)
    suspend fun getTreesForUser(userId: Int) = dao.getTreesForUser(userId)
    suspend fun createTree(tree: TreeEntity) = dao.insertTree(tree)
    suspend fun deleteTree(treeId: Int) = dao.deleteTree(treeId)

    // Members
    fun getMembersForTreeFlow(treeId: Int) = dao.getMembersForTreeFlow(treeId)
    suspend fun getMembersForTree(treeId: Int) = dao.getMembersForTree(treeId)
    fun getMemberFlow(memberId: Int) = dao.getMemberFlow(memberId)
    suspend fun getMemberById(memberId: Int) = dao.getMemberById(memberId)
    suspend fun saveMember(member: MemberEntity) = dao.insertMember(member)
    suspend fun deleteMember(member: MemberEntity) {
        dao.deleteRelationshipsForMember(member.id)
        dao.deleteMember(member)
    }

    // Relationships
    fun getRelationshipsForTreeFlow(treeId: Int) = dao.getRelationshipsForTreeFlow(treeId)
    suspend fun getRelationshipsForTree(treeId: Int) = dao.getRelationshipsForTree(treeId)
    suspend fun saveRelationship(relationship: RelationshipEntity) = dao.insertRelationship(relationship)
    suspend fun deleteRelationship(relationship: RelationshipEntity) = dao.deleteRelationship(relationship)

    // Stories
    fun getStoriesForTreeFlow(treeId: Int) = dao.getStoriesForTreeFlow(treeId)
    fun getStoriesForMemberFlow(memberId: Int) = dao.getStoriesForMemberFlow(memberId)
    suspend fun saveStory(story: StoryEntity) = dao.insertStory(story)
    suspend fun deleteStory(story: StoryEntity) = dao.deleteStory(story)

    // Documents
    fun getDocumentsForTreeFlow(treeId: Int) = dao.getDocumentsForTreeFlow(treeId)
    fun getDocumentsForMemberFlow(memberId: Int) = dao.getDocumentsForMemberFlow(memberId)
    suspend fun saveDocument(document: DocumentEntity) = dao.insertDocument(document)
    suspend fun deleteDocument(document: DocumentEntity) = dao.deleteDocument(document)

    // Complete DB Nuke
    suspend fun nukeDatabase() {
        dao.deleteAllRelationships()
        dao.deleteAllStories()
        dao.deleteAllDocuments()
        dao.deleteAllMembers()
        dao.deleteAllTrees()
        dao.deleteAllUsers()
    }
}
