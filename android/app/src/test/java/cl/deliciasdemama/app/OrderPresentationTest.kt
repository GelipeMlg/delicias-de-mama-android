package cl.deliciasdemama.app

import org.junit.Assert.*
import org.junit.Test

class OrderPresentationTest {
    private val reviewed = Order(customer="Cliente",contact="Contacto",productId="torta",
        quantity=1,date="2026-12-01",time="16:00",flavor="",filling="",theme="",restrictions="",
        status=Status.QUOTED,quote=25000,revision=3)

    @Test fun acceptsOnlyTheReviewedQuote() { requireCurrentQuote(reviewed, reviewed) }

    @Test fun rejectsChangedPriceEvenIfRevisionIsUnchanged() {
        assertTrue(runCatching { requireCurrentQuote(reviewed.copy(quote=30000), reviewed) }.isFailure)
    }

    @Test fun rejectsNewRevisionEvenAtSamePrice() {
        assertTrue(runCatching { requireCurrentQuote(reviewed.copy(revision=4), reviewed) }.isFailure)
    }

    @Test fun rejectsCancelledOrAlreadyConfirmedOrder() {
        listOf(Status.CANCELLED, Status.CONFIRMED, Status.DELIVERED).forEach {
            assertTrue(runCatching { requireCurrentQuote(reviewed.copy(status=it), reviewed) }.isFailure)
        }
    }

    @Test fun rejectsDifferentOrderAndMissingQuote() {
        assertTrue(runCatching { requireCurrentQuote(reviewed.copy(id="other"), reviewed) }.isFailure)
        assertTrue(runCatching { requireCurrentQuote(reviewed.copy(quote=null), reviewed) }.isFailure)
    }

    @Test fun historyContainsOnlyClosedOrders() {
        assertTrue(reviewed.isOpen())
        assertTrue(reviewed.copy(status=Status.CONFIRMED).isOpen())
        assertFalse(reviewed.copy(status=Status.DELIVERED).isOpen())
        assertFalse(reviewed.copy(status=Status.CANCELLED).isOpen())
    }
}
