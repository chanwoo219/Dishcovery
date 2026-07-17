import SwiftUI


struct ToastModifier: ViewModifier {
    let message: String
    @Binding var isShowing: Bool


    func body(content: Content) -> some View {
        ZStack {
            content
            if isShowing {
                VStack {
                    Spacer()
                    Text(message)
                        .padding()
                        .background(Color.black.opacity(0.8))
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .padding(.bottom, 50)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                        .onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                withAnimation { isShowing = false }
                            }
                        }
                }
            }
        }
    }
}


extension View {
    func toast(message: String, isShowing: Binding<Bool>) -> some View {
        modifier(ToastModifier(message: message, isShowing: isShowing))
    }
}
