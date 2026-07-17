import SwiftUI

struct RecipeUploadView: View {
    @State private var title: String = ""
    @State private var rcpDisc: String = ""
    @State private var selectedImage: UIImage? = nil
    @State private var isShowingPicker = false

    var body: some View {
        VStack(spacing: 20) {
            TextField("레시피 제목", text: $title)
                .textFieldStyle(.roundedBorder)

            TextField("설명", text: $rcpDisc)
                .textFieldStyle(.roundedBorder)

            if let img = selectedImage {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFit()
                    .frame(height: 200)
                    .cornerRadius(10)
            }

            Button("이미지 선택") {
                isShowingPicker = true
            }

            Button("등록하기") {
                uploadRecipe()
            }
            .padding()
        }
        .sheet(isPresented: $isShowingPicker) {
            ImagePicker(image: $selectedImage)
        }
        .padding()
    }
}


// MARK: - Upload Function
extension RecipeUploadView {

    func uploadRecipe() {
        guard let url = URL(string: "\(API.baseURL)/api/recipe/upload") else { return }
        guard let image = selectedImage,
              let imageData = image.jpegData(compressionQuality: 0.8) else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        let boundary = "Boundary-\(UUID().uuidString)"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        let body = NSMutableData()

        // 텍스트 데이터
        body.append(convertFormField(named: "title", value: title, using: boundary))
        body.append(convertFormField(named: "rcpDisc", value: rcpDisc, using: boundary))

        // 파일 데이터
        body.append(convertFileData(
            fieldName: "file",
            fileName: "recipe.jpg",
            mimeType: "image/jpeg",
            fileData: imageData,
            using: boundary
        ))

        body.appendString("--\(boundary)--")

        request.httpBody = body as Data

        URLSession.shared.dataTask(with: request) { data, response, error in
            if let err = error {
                print("업로드 실패:", err)
                return
            }
            print("업로드 성공:", String(data: data!, encoding: .utf8) ?? "")
        }.resume()
    }
}



// MARK: - Multipart Helpers
func convertFormField(named name: String, value: String, using boundary: String) -> Data {
    var fieldString = "--\(boundary)\r\n"
    fieldString += "Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n"
    fieldString += "\(value)\r\n"
    return fieldString.data(using: .utf8)!
}

func convertFileData(fieldName: String,
                     fileName: String,
                     mimeType: String,
                     fileData: Data,
                     using boundary: String) -> Data {
    let data = NSMutableData()

    data.appendString("--\(boundary)\r\n")
    data.appendString("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(fileName)\"\r\n")
    data.appendString("Content-Type: \(mimeType)\r\n\r\n")
    data.append(fileData)
    data.appendString("\r\n")

    return data as Data
}


// MARK: - Data append helper
extension NSMutableData {
    func appendString(_ string: String) {
        self.append(string.data(using: .utf8)!)
    }
}
