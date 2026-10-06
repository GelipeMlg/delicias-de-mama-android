package cl.deliciasdemama.app

import org.junit.Assert.*
import org.junit.Test

class CatalogChangesTest {
    private val cake = Product("a", "Torta", "Chocolate", 25000)
    @Test fun newProductHasNoPreviousValue() {
        assertEquals(listOf(null to cake), CatalogChanges.between(emptyList(), listOf(cake)))
    }
    @Test fun editKeepsPreviousValueForConflictDetection() {
        val updated = cake.copy(price = 27000)
        assertEquals(listOf(cake to updated), CatalogChanges.between(listOf(cake), listOf(updated)))
    }
    @Test fun deleteArchivesInsteadOfLosingReferences() {
        assertEquals(listOf(cake to cake.copy(active = false)), CatalogChanges.between(listOf(cake), emptyList()))
    }
    @Test fun unchangedAndAlreadyArchivedProductsDoNotWrite() {
        assertTrue(CatalogChanges.between(listOf(cake), listOf(cake)).isEmpty())
        assertTrue(CatalogChanges.between(listOf(cake.copy(active = false)), emptyList()).isEmpty())
    }
    @Test(expected = IllegalArgumentException::class) fun duplicateIdsAreRejected() {
        CatalogChanges.between(emptyList(), listOf(cake, cake))
    }
}
