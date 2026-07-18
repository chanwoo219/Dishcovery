import SwiftUI

struct AvatarView: View {
    let imgPath: String?
    let size: CGFloat

    var body: some View {
        Circle()
            .fill(Color(.secondarySystemBackground))
            .frame(width: size, height: size)
            .overlay(
                Group {
                    if let path = imgPath {
                        AsyncImage(url: URL(string: (API.baseURL) + path)) { image in
                            image
                                .resizable()
                                .scaledToFill()
                        } placeholder: {
                            placeholder
                        }
                    } else {
                        placeholder
                    }
                }
            )
            .clipShape(Circle())
    }

    private var placeholder: some View {
        Image(systemName: "person.circle.fill")
            .resizable()
            .scaledToFit()
            .foregroundColor(.orange)
    }
}
