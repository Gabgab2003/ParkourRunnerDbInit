package net.downloadpizza.prserver.types

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

fun main() {

}

object Parks : IdTable<String>("p_parks") {
    override val id = varchar("p_id", 45).entityId()

    val longitude = double("longitude")
    val latitude = double("latitude")
}
class Park(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Park>(Parks)

    private var longitude by Parks.longitude
    private var latitude by Parks.latitude

    val coords get() = SimpleCoordinates(this.latitude, this.longitude)
}
