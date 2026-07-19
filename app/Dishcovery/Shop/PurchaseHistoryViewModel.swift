import Foundation

@MainActor
class PurchaseHistoryViewModel: ObservableObject {
    @Published var purchases: [PurchaseHistory] = []
    @Published var myPoint: Int = 0
    @Published var isLoading = false

    var totalQty: Int {
        purchases.map(\.qty).reduce(0, +)
    }

    func load() async {
        isLoading = true
        do {
            purchases = try await ShopApiService.shared.fetchPurchaseHistory()
        } catch {
            print("🔴 [PurchaseHistory] 구매 내역 조회 실패:", error.localizedDescription)
            purchases = []
        }

        do {
            myPoint = try await ShopApiService.shared.fetchMyPoint()
        } catch {
            myPoint = 0
        }
        isLoading = false
    }
}
