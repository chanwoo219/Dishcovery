import Foundation

@MainActor
class ShopListViewModel: ObservableObject {
    @Published var products: [ShopProduct] = []
    @Published var myPoint: Int = 0
    @Published var searchText: String = ""
    @Published var isLoading = false

    func loadProducts() async {
        isLoading = true
        do {
            products = try await ShopApiService.shared.fetchProducts(searchName: searchText.isEmpty ? nil : searchText)
        } catch {
            print("🔴 [Shop] 상품 목록 불러오기 실패:", error.localizedDescription)
        }
        isLoading = false
    }

    func loadMyPoint() async {
        do {
            myPoint = try await ShopApiService.shared.fetchMyPoint()
        } catch {
            myPoint = 0
        }
    }

    func search() {
        Task {
            await loadProducts()
        }
    }
}
