import SwiftUI

@available(iOS 16.0, *)
struct SideMenuView: View {
    @Binding var showMenu: Bool
    @Binding var path: NavigationPath
    @EnvironmentObject var appState: AppState
    
    var body: some View {
        ZStack {
            if showMenu {
                Color.black.opacity(0.35)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(.easeInOut) { showMenu = false }
                    }
            }
            
            HStack(spacing: 0) {
                VStack(alignment: .leading, spacing: 25) {
                    
                    Text("메뉴")
                        .font(.title2.bold())
                        .padding(.bottom, 10)
                    
                    MenuItem(label: "홈", system: "house") {
                        withAnimation { showMenu = false }
                    }
                    
                    if appState.isLoggedIn {
                        
                        MenuItem(label: "레시피 등록", system: "pencil") {
                            withAnimation {
                                showMenu = false
                                path.append(Page.recipeWrite)
                            }
                        }
                        
                        MenuItem(label: "상점", system: "cart") {
                            withAnimation {
                                showMenu = false
                                path.append(Page.shop)
                            }
                        }

                        MenuItem(label: "마이페이지", system: "person.circle") {
                            withAnimation {
                                showMenu = false
                                path.append(Page.myPage)
                            }
                        }

                    }
                    

                    MenuItem(label: "AI 레시피추천", system: "sparkles") {
                        withAnimation {
                            showMenu = false
                            path.append(Page.aiRecipe)
                        }
                    }
                    
                    Spacer()
                }
                .padding(.top, 60)
                .padding(.horizontal, 20)
                .frame(maxWidth: 260, maxHeight: .infinity)
                .background(.ultraThinMaterial)
                .offset(x: showMenu ? 0 : -300)
                .animation(.easeInOut(duration: 0.25), value: showMenu)
                
                Spacer()
            }
        }
        .ignoresSafeArea()
    }
}

struct MenuItem: View {
    let label: String
    let system: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 15) {
                Image(systemName: system)
                    .font(.system(size: 18))
                    .foregroundColor(.orange)
                
                Text(label)
                    .foregroundColor(.orange)
                    .font(.system(size: 18, weight: .medium))
            }
            .padding(.vertical, 8)
        }
    }
}
