package cl.deliciasdemama.app

import org.junit.Assert.*
import org.junit.Test

class AccessRulesTest {
    private val client = Identity("cliente-a", "a@example.test", false)
    private val owner = Identity("duena", "d@example.test", true)
    private fun order(user: String?) = Order(customer="Prueba",contact="Ficticio",productId="torta",quantity=1,
        date="2026-09-30",time="15:00",flavor="",filling="",theme="",restrictions="",customerUserId=user)
    private fun state() = ShopState(orders=listOf(order(client.id),order("otro"),order(null)),
        expenses=listOf(Expense(concept="Privado",amount=1000,date="2026-09-22",category="otro")),
        sales=listOf(Sale(concept="Privado",amount=5000,date="2026-09-22")),
        products=demoCatalog+Product("oculto","Retirado","Interno",1000,false))

    @Test fun guestCannotBecomeOwner() {
        assertThrows(IllegalArgumentException::class.java){AccessRules.requireOwner(null)}
        assertThrows(IllegalArgumentException::class.java){AccessRules.requireOwner(client)}
        AccessRules.requireOwner(owner)
    }
    @Test fun anonymousProjectionHidesAllPrivateData() {
        val visible=AccessRules.visible(state(),null)
        assertTrue(visible.orders.isEmpty());assertTrue(visible.sales.isEmpty());assertTrue(visible.expenses.isEmpty())
        assertFalse(visible.products.any{!it.active})
    }
    @Test fun clientOnlySeesOwnOrdersAndNoFinance() {
        val visible=AccessRules.visible(state(),client)
        assertEquals(1,visible.orders.size);assertEquals(client.id,visible.orders.single().customerUserId)
        assertTrue(visible.sales.isEmpty());assertTrue(visible.expenses.isEmpty())
    }
    @Test fun legacyOrdersNeverBecomePropertyOfFirstLogin() {
        val initial=state()
        assertFalse(AccessRules.visible(initial,client).orders.any{it.customerUserId==null})
        assertEquals(initial,AccessRules.visible(initial,owner))
    }
    @Test fun clientCannotModifyOtherClientsOrLegacyOrders() {
        val s=state()
        AccessRules.requireOrder(s,client,s.orders.first().id)
        s.orders.drop(1).forEach{order->assertThrows(IllegalArgumentException::class.java){AccessRules.requireOrder(s,client,order.id)}}
        assertThrows(IllegalArgumentException::class.java){AccessRules.requireOrder(s,null,s.orders.first().id)}
        assertThrows(IllegalArgumentException::class.java){AccessRules.requireOrder(s,owner,"inexistente")}
    }
    @Test fun ownerCanManageAllExistingOrders() {
        val s=state();s.orders.forEach{AccessRules.requireOrder(s,owner,it.id)}
    }
    @Test fun projectionDoesNotDeletePrivateRepositoryData() {
        val initial=state();AccessRules.visible(initial,null)
        assertEquals(3,initial.orders.size);assertEquals(1,initial.expenses.size);assertEquals(1,initial.sales.size)
    }
    @Test fun passwordLimitsUseUtf8BytesForBcrypt() {
        AccessRules.validatePassword("UnaFraseDePrueba123")
        assertThrows(IllegalArgumentException::class.java){AccessRules.validatePassword("corta")}
        assertThrows(IllegalArgumentException::class.java){AccessRules.validatePassword("a".repeat(73))}
        assertThrows(IllegalArgumentException::class.java){AccessRules.validatePassword("á".repeat(37))}
        AccessRules.validatePassword("á".repeat(36))
    }
}
