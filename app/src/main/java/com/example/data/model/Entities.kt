package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val phone: String,
    val fullName: String,
    val passwordHash: String,
    val isSessionActive: Boolean = false
)

@Entity(
    tableName = "trees",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TreeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String,
    val isPrivate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["treeId"])]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val treeId: Int,
    val firstName: String,
    val lastName: String,
    val gender: String, // "Male", "Female", "Other"
    val birthDate: String,
    val birthLocation: String? = null,
    val isDeceased: Boolean = false,
    val deathDate: String? = null,
    val deathLocation: String? = null,
    val bio: String = "",
    val occupation: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val photoUri: String? = null,
    val generation: Int = 1
)

@Entity(
    tableName = "relationships",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedMemberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["treeId"]),
        Index(value = ["memberId"]),
        Index(value = ["relatedMemberId"])
    ]
)
data class RelationshipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val treeId: Int,
    val memberId: Int, // Parent or Spouse from
    val relatedMemberId: Int, // Child or Spouse to
    val relationshipType: String, // "PARENT_OF", "SPOUSE_OF"
    val details: String? = null
)

@Entity(
    tableName = "stories",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["treeId"]),
        Index(value = ["memberId"])
    ]
)
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val treeId: Int,
    val memberId: Int,
    val title: String,
    val content: String,
    val category: String = "Story", // "Milestone", "Story", "Achievement"
    val dateOccurred: String? = null,
    val mediaUri: String? = null
)

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["treeId"]),
        Index(value = ["memberId"])
    ]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val treeId: Int,
    val memberId: Int,
    val title: String,
    val description: String? = null,
    val fileType: String = "Document", // "Certificate", "Will", "Letter", "Photo"
    val fileUri: String,
    val dateAdded: Long = System.currentTimeMillis()
)
