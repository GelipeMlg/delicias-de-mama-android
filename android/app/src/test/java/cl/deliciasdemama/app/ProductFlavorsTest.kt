package cl.deliciasdemama.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ProductFlavorsTest {
    private val cake = Product("cake", "Pastel", "A elección", 20000, flavorsEnabled=true, flavors=listOf("Chocolate", "Vainilla"))
    @Test fun saltedProductDoesNotRequireFlavors() {
        val empanadas = Product("salado", "Empanadas pequeñas", "Cóctel salado", 10000)
        assertFalse(ShopRules.product(ShopState(),empanadas).products.last().flavorsEnabled)
    }
    @Test(expected=IllegalArgumentException::class) fun enabledFlavorsCannotBeEmpty() {
        ShopRules.product(ShopState(), cake.copy(flavors=emptyList()))
    }
    @Test fun flavorsAreTrimmedAndDeduplicated() {
        val result=ShopRules.product(ShopState(),cake.copy(flavors=listOf(" Chocolate ","chocolate","Vainilla")))
        assertEquals(listOf("Chocolate","Vainilla"),result.products.last().flavors)
    }
    @Test(expected=IllegalArgumentException::class) fun multipleAvailableFlavorsAreRejected() {
        val order=Order(customer="Prueba",contact="Prueba",productId="cake",quantity=1,date="2030-01-01",time="12:00",flavor="Chocolate · Vainilla",filling="",theme="",restrictions="")
        val state=ShopState(products=listOf(cake))
        assertEquals(order.flavor,ShopRules.request(state,order,LocalDate.of(2029,1,1)).orders.single().flavor)
    }
    @Test(expected=IllegalArgumentException::class) fun unknownFlavorIsRejected() {
        ShopRules.request(ShopState(products=listOf(cake)),Order(customer="Prueba",contact="Prueba",productId="cake",quantity=1,date="2030-01-01",time="12:00",flavor="No disponible",filling="",theme="",restrictions=""),LocalDate.of(2029,1,1))
    }
    @Test fun oneFlavorAndOneFillingAndPortionAreAccepted() {
        val product=cake.copy(fillings=listOf("Manjar","Crema"),servings=listOf(10,20))
        val order=Order(customer="Cliente",contact="Contacto",productId="cake",quantity=1,date="2030-01-01",time="12:00",flavor="Chocolate",filling="Manjar",theme="",restrictions="",servings=20)
        assertEquals(20,ShopRules.request(ShopState(products=listOf(product)),order,LocalDate.of(2029,1,1)).orders.single().servings)
        assertThrows(IllegalArgumentException::class.java) { ShopRules.request(ShopState(products=listOf(product)),order.copy(filling="Manjar · Crema"),LocalDate.of(2029,1,1)) }
        assertThrows(IllegalArgumentException::class.java) { ShopRules.request(ShopState(products=listOf(product)),order.copy(servings=15),LocalDate.of(2029,1,1)) }
    }
    @Test fun portionsArePreservedWhenCakeModeIsDisabled() {
        val result=ShopRules.product(ShopState(),cake.copy(flavorsEnabled=false,fillings=listOf("Manjar"),servings=listOf(20,10,20))).products.last()
        assertEquals(listOf(10,20),result.servings)
        assertTrue(result.flavors.isEmpty())
        assertTrue(result.fillings.isEmpty())
    }
    @Test fun retiredProductCanStillBeSentForDeletion() {
        val retired = cake.copy(active=false)
        assertEquals(listOf(retired),CatalogChanges.removed(listOf(retired),emptyList()))
    }
}
