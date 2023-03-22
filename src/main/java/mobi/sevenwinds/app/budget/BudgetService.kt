package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId.id.value
                this.createdAt = DateTime.now()
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val filter = BudgetTable.year.eq(param.year)
            val authorFilter = AuthorTable.name.lowerCase().like("%${param.authorNameFilter.toLowerCase()}%")
//            val query = BudgetTable
//                .select { BudgetTable.year eq param.year }
//                .limit(param.limit, param.offset)
            val query = (BudgetTable innerJoin AuthorTable)
                .slice(BudgetTable.columns + AuthorTable.name + AuthorTable.createdAt)
                .select { filter and authorFilter }
                .orderBy(BudgetTable.createdAt)

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }
                .sortedWith(compareBy ({ it.month }, {-it.amount}))

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}