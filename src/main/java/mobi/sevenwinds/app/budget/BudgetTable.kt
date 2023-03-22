package mobi.sevenwinds.app.budget

import javassist.NotFoundException
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime
import java.time.LocalDateTime

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val authorId = integer("author_id")
    val createdAt = datetime("created_at")
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var authorId by BudgetTable.authorId
    var createdAt by BudgetTable.createdAt

    fun toResponse(): BudgetRecord {
        val author = authorId.let {
            AuthorEntity.findById(it) ?: throw NotFoundException("Автор не найден")
        }
        //val authorRecord = AuthorEntity.findById(authorId)?.toResponse()
        return BudgetRecord(year, month, amount, type, author, createdAt)
    }
}

//data class BudgetRecordWithAuthor(
//    val year: Int,
//    val month: Int,
//    val amount: Int,
//    val type: BudgetType,
//    val author: AuthorRecord?,
//    val createdAt: DateTime
//)

