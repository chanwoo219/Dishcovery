import Foundation

@MainActor
class ShopDetailViewModel: ObservableObject {
    @Published var product: ShopProduct?
    @Published var myPoint: Int = 0
    @Published var reviews: [ShopReview] = []
    @Published var inquiries: [ShopInquiry] = []
    @Published var recommended: [ShopProduct] = []

    @Published var reviewContent: String = ""
    @Published var reviewRating: Int = 5
    @Published var inquiryContent: String = ""

    @Published var toastMessage = ""
    @Published var showToast = false

    var averageRating: Double {
        guard !reviews.isEmpty else { return 0 }
        return Double(reviews.map(\.rating).reduce(0, +)) / Double(reviews.count)
    }

    func load(productId: String) async {
        do {
            product = try await ShopApiService.shared.fetchProduct(productId: productId)
        } catch {
            print("🔴 [Shop] 상품 상세 불러오기 실패:", error.localizedDescription)
        }

        await loadReviews(productId: productId)
        await loadInquiries(productId: productId)

        do {
            recommended = try await ShopApiService.shared.fetchRecommended(productId: productId)
        } catch {
            recommended = []
        }

        do {
            myPoint = try await ShopApiService.shared.fetchMyPoint()
        } catch {
            myPoint = 0
        }
    }

    func loadReviews(productId: String) async {
        do {
            reviews = try await ShopApiService.shared.fetchReviews(productId: productId)
        } catch {
            reviews = []
        }
    }

    func loadInquiries(productId: String) async {
        do {
            inquiries = try await ShopApiService.shared.fetchInquiries(productId: productId)
        } catch {
            inquiries = []
        }
    }

    func purchase(productId: String) {
        Task {
            do {
                let message = try await ShopApiService.shared.purchase(productId: productId, qty: 1)
                toastMessage = message
                showToast = true
                myPoint = (try? await ShopApiService.shared.fetchMyPoint()) ?? myPoint
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }

    func submitReview(productId: String) {
        Task {
            do {
                let message = try await ShopApiService.shared.addReview(
                    productId: productId, rating: reviewRating, content: reviewContent)
                toastMessage = message
                showToast = true
                reviewContent = ""
                await loadReviews(productId: productId)
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }

    func submitInquiry(productId: String) {
        Task {
            do {
                let message = try await ShopApiService.shared.addInquiry(productId: productId, content: inquiryContent)
                toastMessage = message
                showToast = true
                inquiryContent = ""
                await loadInquiries(productId: productId)
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
