package cl.deliciasdemama.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ShopRulesTest {
    private val today=LocalDate.of(2026,9,16)
    private fun request()=Order(customer="Cliente de prueba",contact="Contacto ficticio",productId="torta",quantity=1,date="2026-09-20",time="15:00",flavor="",filling="",theme="",restrictions="")
    private fun quoted(): ShopState {
        val order=request()
        return ShopRules.quote(ShopRules.request(ShopState(),order,today),order.id,20000)
    }
    @Test fun onlyOneOrderCanTakeTheLastSlot() {
        val empty=ShopRules.configure(ShopState(),DayConfig("2026-09-20",1,false))
        val state=ShopRules.request(empty,request(),today)
        assertEquals(1,ShopRules.used(state,"2026-09-20"))
        assertFalse(ShopRules.available(state,"2026-09-20"))
        assertThrows(IllegalArgumentException::class.java){ShopRules.request(state,request(),today)}
    }
    @Test fun cancellationFreesCapacity() {
        var state=quoted(); val id=state.orders.first().id
        state=ShopRules.configure(state,DayConfig("2026-09-20",1,false))
        state=ShopRules.confirm(state,id,today)
        assertFalse(ShopRules.available(state,"2026-09-20"))
        assertTrue(ShopRules.available(ShopRules.cancel(state,id),"2026-09-20"))
    }
    @Test fun cannotAcceptOnBlockedDay() {
        val state=ShopRules.configure(quoted(),DayConfig("2026-09-20",3,true))
        assertThrows(IllegalArgumentException::class.java){ShopRules.confirm(state,state.orders.first().id,today)}
    }
    @Test fun cannotDeliverTwice() {
        var state=quoted();val id=state.orders.first().id
        state=ShopRules.deliver(ShopRules.confirm(state,id,today),id,today)
        assertEquals("2026-09-16",state.orders.first().saleDate)
        assertThrows(IllegalArgumentException::class.java){ShopRules.deliver(state,id,today)}
    }
    @Test fun cannotReduceCapacityBelowConfirmedOrders() {
        val initial=quoted();val state=ShopRules.confirm(initial,initial.orders.first().id,today)
        assertThrows(IllegalArgumentException::class.java){ShopRules.configure(state,DayConfig("2026-09-20",0,false))}
    }
    @Test fun invalidAmountsAndPastDatesAreRejected() {
        assertThrows(IllegalArgumentException::class.java){ShopRules.expense(ShopState(),Expense(concept="Harina",amount=-1,date=today.toString(),category="insumos"))}
        assertThrows(IllegalArgumentException::class.java){ShopRules.request(ShopState(),request().copy(date="2026-09-15"),today)}
    }
    @Test fun quoteCorrectionRequiresAcceptanceAndKeepsOneSlot() {
        val initial=quoted(); val id=initial.orders.first().id
        val corrected=ShopRules.quote(ShopRules.confirm(initial,id,today),id,26000)
        assertEquals(Status.QUOTED,corrected.orders.first().status)
        assertEquals(26000L,corrected.orders.first().quote)
        assertEquals(1,ShopRules.used(corrected,"2026-09-20"))
        assertThrows(IllegalArgumentException::class.java){ShopRules.deliver(corrected,id,today)}
        assertEquals(Status.CONFIRMED,ShopRules.confirm(corrected,id,today).orders.first().status)
    }
    @Test fun pendingCancellationReleasesSlotAndCapacityCannotIgnoreRequests() {
        val state=ShopRules.request(ShopState(),request(),today)
        assertThrows(IllegalArgumentException::class.java){ShopRules.configure(state,DayConfig("2026-09-20",0,false))}
        assertEquals(0,ShopRules.used(ShopRules.cancel(state,state.orders.first().id),"2026-09-20"))
    }
    @Test fun productCrudAndRetirementPreserveOrderHistory() {
        val product=Product("nuevo","Galletas","Decoradas",8000)
        var state=ShopRules.product(ShopState(),product)
        state=ShopRules.request(state,request().copy(productId=product.id),today)
        state=ShopRules.product(state,product.copy(name="Galletas premium",price=10000))
        assertEquals("Galletas",state.orders.first().productName)
        assertEquals(10000L,state.products.first{it.id==product.id}.price)
        state=ShopRules.deleteProduct(state,product.id)
        assertFalse(state.products.first{it.id==product.id}.active)
        assertThrows(IllegalArgumentException::class.java){ShopRules.request(state,request().copy(productId=product.id),today)}
        state=ShopRules.product(state,product)
        assertTrue(state.products.first{it.id==product.id}.active)
        assertFalse(ShopRules.deleteProduct(ShopState(),"cupcakes").products.any{it.id=="cupcakes"})
    }
    @Test fun expenseCrudReplacesAmountAndDateWithoutDuplication() {
        val expense=Expense(concept="Harina",amount=3000,date=today.toString(),category="insumos")
        var state=ShopRules.expense(ShopState(),expense)
        state=ShopRules.expense(state,expense.copy(amount=4500,date="2026-09-15",category="otro"))
        assertEquals(1,state.expenses.size)
        assertEquals(4500L,state.expenses.sumOf{it.amount})
        assertEquals("2026-09-15",state.expenses.single().date)
        assertTrue(ShopRules.deleteExpense(state,expense.id).expenses.isEmpty())
    }
    @Test fun deliveredSaleCanBeCorrectedWithoutChangingClosedQuoteOrCapacity() {
        val initial=quoted(); val id=initial.orders.first().id
        var state=ShopRules.deliver(ShopRules.confirm(initial,id,today),id,today)
        val sale=state.sales.single()
        assertEquals(id,sale.orderId)
        state=ShopRules.sale(state,sale.copy(amount=19000))
        assertEquals(19000L,state.sales.single().amount)
        assertEquals(20000L,state.orders.single().quote)
        assertThrows(IllegalArgumentException::class.java){ShopRules.quote(state,id,18000)}
        state=ShopRules.deleteSale(state,sale.id)
        assertTrue(state.sales.isEmpty())
        assertEquals(Status.DELIVERED,state.orders.single().status)
        assertEquals(1,ShopRules.used(state,"2026-09-20"))
    }
    @Test fun manualSalesCrudAndInvalidFinancialInput() {
        val sale=Sale(concept="Mostrador",amount=5000,date=today.toString())
        val created=ShopRules.sale(ShopState(),sale)
        val edited=ShopRules.sale(created,sale.copy(amount=6000))
        assertEquals(6000L,edited.sales.single().amount)
        assertTrue(ShopRules.deleteSale(edited,sale.id).sales.isEmpty())
        assertThrows(IllegalArgumentException::class.java){ShopRules.sale(created,sale.copy(amount=0))}
        assertThrows(IllegalArgumentException::class.java){ShopRules.sale(created,sale.copy(orderId="otro"))}
        assertThrows(java.time.format.DateTimeParseException::class.java){ShopRules.sale(created,sale.copy(date="2026-02-30"))}
    }
}
