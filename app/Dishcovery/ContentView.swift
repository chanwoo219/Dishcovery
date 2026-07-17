import SwiftUI

enum Page: Hashable {
    case signup
    case login
    case main
    case aiRecipe
    case recipeWrite
    case myRecipe
    case recipeDetail(recipeId: String)
}

@available(iOS 16.0, *)
struct ContentView: View {
    @StateObject var viewModel = RecipeViewModel()
    
    @State private var showMenu = false
    @State private var path = NavigationPath()
    
    @EnvironmentObject var appState: AppState
    
    var body: some View {
        NavigationStack(path: $path) {
            
            // Bridge a single selection binding for views that expect Binding<Page?>
            let selectedPage = Binding<Page?>(
                get: { nil },
                set: { newValue in
                    if let page = newValue {
                        path.append(page)
                    } else {
                        path.removeLast(path.count)
                    }
                }
            )
            
            ZStack {
                
                // MAIN CONTENT
                VStack(spacing: 0) {
                    // 상단 헤더
                    HStack {
                        // 햄버거 메뉴 버튼
                        Button(action: {
                            withAnimation {
                                showMenu.toggle()
                            }
                        }) {
                            VStack(spacing: 4) {
                                Rectangle().frame(width: 25, height: 3)
                                Rectangle().frame(width: 25, height: 3)
                                Rectangle().frame(width: 25, height: 3)
                            }
                            .foregroundColor(.orange)
                        }
                        
                        Text("Dishcovery")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                            .padding(.leading, 8)
                        
                        Spacer()
                        
                        if appState.isLoggedIn {
                            HStack(spacing: 4) {
                                Image("profile_icon")
                                    .resizable()
                                    .frame(width: 24, height: 24)
                                    .clipShape(Circle())
                                
                                Text(" \(appState.username)님")
                                    .font(.body)
                                    .foregroundColor(.orange)
                                
                                Button(action: {
                                    appState.isLoggedIn = false
                                    appState.username = ""
                                    UserDefaults.standard.removeObject(forKey: "JWT_TOKEN")
                                    UserDefaults.standard.removeObject(forKey: "USERNAME")
                                }) {
                                    Text("로그아웃")
                                        .font(.subheadline)
                                        .foregroundColor(.white)
                                        .padding(.vertical, 6)
                                        .padding(.horizontal, 6)
                                        .background(Color.orange)
                                        .cornerRadius(8)
                                }
                            }
                            
                        } else {
                            NavigationLink(value: Page.login) {
                                Text("로그인")
                                    .font(.headline)
                                    .foregroundColor(.white)
                                    .padding(.vertical, 8)
                                    .padding(.horizontal, 16)
                                    .background(Color.orange)
                                    .cornerRadius(10)
                            }
                        }
                        
                    }
                    .padding(.horizontal)
                    .padding(.top, 16)
                    .padding(.bottom, 15)
                    
                    // 레시피 리스트
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                            ForEach(viewModel.recipes) { recipe in
                                Button {
                                    path.append(Page.recipeDetail(recipeId: recipe.recipeId))
                                } label: {
                                    RecipeCardView(recipe: recipe)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding(.horizontal)
                        .padding(.top)
                    }
                }
                .task {
                    await viewModel.fetchRecipes()
                }
                
                //사이드메뉴 수정
                SideMenuView(showMenu: $showMenu, path: $path)
            }
            
            
            // 페이지 이동 처리
            .navigationDestination(for: Page.self) { page in
                switch page {
                case .login:
                    LoginView(route: selectedPage, viewModel: LoginViewModel(appState: appState))
                        .navigationTitle("로그인")
                        .navigationBarTitleDisplayMode(.inline)
                case .signup:
                    SignupView()
                        .navigationTitle("회원가입")
                        .navigationBarTitleDisplayMode(.inline)
                case .recipeWrite:
                    RecipeRegisterView(path: $path)
                        .navigationTitle("레시피 등록")
                        .navigationBarTitleDisplayMode(.inline)
                    
                case .aiRecipe:
                    RecipeAiView()
                        .navigationTitle("AI 레시피추천")
                        .navigationBarTitleDisplayMode(.inline)
                case .myRecipe:
                    MyRecipeView(path: $path)
                        .navigationTitle("나의 레시피")
                        .navigationBarTitleDisplayMode(.inline)
                case .main:
                    EmptyView()
                        .onAppear { path.removeLast(path.count) }
                        .navigationTitle("메인")
                        .navigationBarTitleDisplayMode(.inline)
                case .recipeDetail(let recipeId):
                    if let recipe = viewModel.recipes.first(where: { $0.recipeId == recipeId }) {
                        RecipeDetailView(recipe: recipe)
                    } else {
                        Text("레시피를 찾을 수 없습니다.")
                    }
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        if #available(iOS 16.0, *) {
            ContentView()
                .environmentObject(AppState())
        } else {
            // Fallback on earlier versions
        }
    }
}

