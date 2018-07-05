package example

import org.sql2o.Connection
import org.sql2o.Sql2o
import java.sql.DriverManager

val DB_URL = "jdbc:h2:mem:mydb"
private val USERNAME = "sa"
private val PASSWORD = "sa"


fun main(args: Array<String>) {
    prepareDb()
    val sql2o = Sql2o(DB_URL, USERNAME, PASSWORD)
    sql2o.open().use { conn ->
        insert(conn)
        query(conn)
        update(conn)
        query(conn)
        delete(conn)
        query(conn)
    }
}

fun delete(conn: Connection) {
    conn.createQuery("""delete from mytbl where id=1""").executeUpdate()
}

fun update(conn: Connection) {
    conn.createQuery("""update mytbl set name = 'Hi!'""").executeUpdate()
}

fun insert(conn: Connection) {
    val data = mapOf(1 to "hello", 2 to "world")
    data.forEach { (id, name) ->
        conn.createQuery("""insert into mytbl values(:id, :name)""")
                .addParameter("id", id)
                .addParameter("name", name)
                .executeUpdate()
    }
}

private fun query(conn: Connection) {
    val sql = """select * from mytbl"""
    val users = conn.createQuery(sql).executeAndFetch(User::class.java)
    println(users)
}

data class User(val id: Int, val name: String)

private fun prepareDb() {
    Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)
    conn.createStatement().use { stmt ->
        with(stmt) {
            executeUpdate("create table mytbl(id int primary key, name varchar(255))")
        }
    }
    // Notice
    // the `conn` should not be closed, otherwise the db will be destroyed
}

fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        try {
            this?.close()
        } catch (e: Exception) {
            println(e.toString())
        }
    }
}