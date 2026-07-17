import SwiftUI
import UIKit

struct ImagePickerView: View {

    @Binding var selectedImageData: Data?
    @State private var showPicker = false
    @State private var selectedImage: UIImage?

    var body: some View {
        VStack(alignment: .leading) {
            Text("대표 이미지")
                .font(.headline)

            Button(action: { showPicker = true }) {
                if let data = selectedImageData, let uiImage = UIImage(data: data) {
                    Image(uiImage: uiImage)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 150, height: 150)
                        .clipped()
                        .cornerRadius(10)
                } else {
                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(Color.gray, style: StrokeStyle(lineWidth: 1, dash: [5]))
                            .frame(width: 150, height: 150)
                        Text("이미지 선택")
                            .foregroundColor(.gray)
                    }
                }
            }
            .sheet(isPresented: $showPicker) {
                LegacyImagePicker(image: $selectedImage)
            }
            .onChange(of: selectedImage) { newValue in
                guard let uiImage = newValue else { return }
                // 서버 전송용 Data (필요하면 pngData()로 변경 가능)
                selectedImageData = uiImage.jpegData(compressionQuality: 0.9)
            }
        }
    }
}
