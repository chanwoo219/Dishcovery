import SwiftUI
import UIKit

class RecipeRegisterViewModel: ObservableObject {
    @Published var title: String = ""
    @Published var categoryId: String = ""
    @Published var cookTime: String = ""
    @Published var cookDfct: String = ""
    @Published var rcpDisc: String = ""
    @Published var recipeIngr: String = ""
    @Published var recipeTip: String = ""
    @Published var recipeTag: String = ""
    @Published var stepDescriptions: [String] = [""]

    @Published var selectedImage: UIImage?
    @Published var imageData: Data?   // ← 최종 서버로 보낼 이미지 Data 저장

    // Toast
   // @Published var authToken: String? // 로그인 성공 후 저장
    @AppStorage("JWT_TOKEN") private var authToken: String?
    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var shouldNavigateToMain = false
   
    @Published var categories: [CodeVo] = []

    
    private let categoryService = CategoryApiService()

    init() {
        Task {
            await loadCategories()
        }
    }

    func loadCategories() async {
        do {
            let list = try await categoryService.fetchCategories()
            self.categories = list
        } catch {
            print("카테고리 조회 실패:", error)
        }
    }
    
    
    // PhotosPicker에서 이미지 선택 시 호출됨
    func updateImageData() {
        if let img = selectedImage {
            imageData = img.jpegData(compressionQuality: 0.8)
        }
    }

    func submitRecipe() {
        
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
            toastMessage = "로그인이 필요합니다"
            showToast = true
            return
        }

        updateImageData()  // UIImage → Data 변환

        let recipe = RecipeUploadRequest(
            title: title,
            categoryId: categoryId,
            cookTime: cookTime,
            cookDfct: cookDfct,
            rcpDisc: rcpDisc,
            recipeIngr: recipeIngr,
            recipeTip: recipeTip,
            recipeTag: recipeTag,
            stepDescriptions: stepDescriptions,
            imageData: imageData
        )

        RecipeApiService.shared.uploadRecipe(recipe, token: token) { success, message in
            DispatchQueue.main.async {
                self.toastMessage = message
                self.showToast = true

                if success {
                    // 토스트를 잠깐 보여준 뒤 메인 화면으로 이동
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                        self.shouldNavigateToMain = true
                    }
                }
            }
        }
    }
}
