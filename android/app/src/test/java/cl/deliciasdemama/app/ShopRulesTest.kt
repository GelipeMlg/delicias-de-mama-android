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
        val first=quoted(); val second=request()
        var state=ShopRules.request(first,second,today)
        state=ShopRules.quote(state,second.id,15000)
        state=ShopRules.configure(state,DayConfig("2026-09-20",1,false))
        state=ShopRules.confirm(state,first.orders.first().id,today)
        assertThrows(IllegalArgumentException::class.java){ShopRules.confirm(state,second.id,today)}
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
}
