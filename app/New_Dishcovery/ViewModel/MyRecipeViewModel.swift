//
//  MyRecipeViewModel.swift
//  New_Dishcovery
//
//  Created by 박진희 on 2025/12/18.
//

import Foundation

@MainActor
class MyRecipeViewModel: ObservableObject {
    @Published var myRecipes: [Recipe] = []

    func fetchMyRecipes() async {
        print("🟡 [MyRecipe] fetchMyRecipes 호출됨")



        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
            print("🔴 [MyRecipe] JWT_TOKEN 없음")
            return
        }

        print("🟢 [MyRecipe] JWT_TOKEN 있음")

        guard let url = URL(string: "\(API.baseURL)/api/recipes/myRecipe") else {
            print("🔴 [MyRecipe] URL 생성 실패")
            return
        }

        print("🟢 [MyRecipe] 요청 URL:", url.absoluteString)

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        do {
            print("🟡 [MyRecipe] 서버 요청 시작")

            let (data, response) = try await URLSession.shared.data(for: request)

            if let httpResponse = response as? HTTPURLResponse {
                print("🟢 [MyRecipe] 응답 상태 코드:", httpResponse.statusCode)
            }

            print("🟢 [MyRecipe] raw data size:", data.count)

            // 🔍 서버에서 내려준 원본 JSON 확인
            if let jsonString = String(data: data, encoding: .utf8) {
                print("📦 [MyRecipe] 서버 응답 JSON:")
                print(jsonString)
            }

            let decoded = try JSONDecoder().decode([Recipe].self, from: data)
            self.myRecipes = decoded

            print("✅ [MyRecipe] 디코딩 성공, 레시피 개수:", decoded.count)

        } catch {
            print("❌ [MyRecipe] 레시피 불러오기 실패")
            print(error.localizedDescription)
        }
    }
}
