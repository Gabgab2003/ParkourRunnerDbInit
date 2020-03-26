package net.downloadpizza.prserver.types

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import com.beust.klaxon.JsonParsingException
import com.beust.klaxon.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

const val jdbc = "jdbc:mysql://root:@localhost:8889"
const val driver = "com.mysql.cj.jdbc.Driver"

fun main() {
    val klaxon = Klaxon()
    val parks = run {
        val stream = object {}.javaClass.getResourceAsStream("PARKINFOOGD.json");
        val parkFile = klaxon.parse<ParkFile>(stream) ?: throw JsonParsingException("Failed to parse parks")

        if (parkFile.totalFeatures != parkFile.features.size) {
            System.err.println("WARNING: Indicated number of features (${parkFile.totalFeatures} not the same as real number ${parkFile.features.size})")
        }

        parkFile.features
    }

    val database = Database.connect(jdbc, driver, "root", "sqlpassword")
    val nameMap = emptyMap<String, Int>().toMutableMap()

    transaction(database) {
        parks.forEach {
            var name = it.properties.institutionName
            val num = nameMap[name]
            if (num != null) {
                name += " $num"
                nameMap[name] = num + 1
            } else if (parks.any { c ->
                    c !== it && c.properties.institutionName == name
                }) {
                nameMap[name] = 2
                name += " 1"
            }
            Park.new(it.id) {
                this.name = name
                this.latitude = it.geometry.coordinates[0]
                this.longitude = it.geometry.coordinates[1]
            }
        }
    }
}

object Parks : IdTable<String>("p_parks") {
    override val id = varchar("p_id", 45).entityId()
    val name = varchar("p_name", 60)
    val longitude = double("p_longitude")
    val latitude = double("p_latitude")
}

class Park(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Park>(Parks)

    var name by Parks.name
    var longitude by Parks.longitude
    var latitude by Parks.latitude
}
