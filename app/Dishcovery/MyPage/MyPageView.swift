import SwiftUI
import UIKit

@available(iOS 16.0, *)
struct MyPageView: View {
    @StateObject private var viewModel = MyPageViewModel()
    @Binding var path: NavigationPath
    @EnvironmentObject var appState: AppState
    @State private var showImagePicker = false
    @State private var pickedImage: UIImage?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {

                // 프로필 카드
                VStack(spacing: 10) {
                    Button {
                        showImagePicker = true
                    } label: {
                        ZStack(alignment: .bottomTrailing) {
                            AvatarView(imgPath: viewModel.profile?.userImgPath, size: 80)

                            Image(systemName: "camera.circle.fill")
                                .font(.system(size: 24))
                                .foregroundColor(.orange)
                                .background(Circle().fill(.white))
                        }
                    }
                    .sheet(isPresented: $showImagePicker) {
                        LegacyImagePicker(image: $pickedImage)
                    }
                    .onChange(of: pickedImage) { newValue in
                        if let image = newValue {
                            viewModel.uploadProfileImage(image)
                        }
                    }

                    if let profile = viewModel.profile {
                        Text(profile.userId)
                            .font(.headline)
                        Text(profile.userMail)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text("포인트 \(profile.pointBalance)P")
                            .font(.subheadline)
                            .foregroundColor(.orange)
                    }

                    Button {
                        path.append(Page.changeNickname)
                    } label: {
                        Text("닉네임 변경")
                            .font(.subheadline)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(Color.orange)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                    .padding(.top, 4)
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(15)

                // 내가 등록한 레시피
                VStack(alignment: .leading, spacing: 10) {
                    Text("내가 등록한 레시피")
                        .font(.headline)

                    if viewModel.myRecipes.isEmpty {
                        Text("아직 등록한 레시피가 없어요.")
                            .foregroundColor(.secondary)
                    } else {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(viewModel.myRecipes) { recipe in
                                    Button {
                                        path.append(Page.recipeDetail(recipe: recipe))
                                    } label: {
                                        RecipeCardView(recipe: recipe)
                                            .frame(width: 160)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                        }
                    }
                }

                // 추천 유저
                VStack(alignment: .leading, spacing: 10) {
                    Text("추천 유저")
                        .font(.headline)

                    if viewModel.recommendedUsers.isEmpty {
                        Text("추천할 유저가 없어요.")
                            .foregroundColor(.secondary)
                    } else {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 16) {
                                ForEach(viewModel.recommendedUsers) { user in
                                    Button {
                                        path.append(Page.publicProfile(userId: user.userId))
                                    } label: {
                                        VStack(spacing: 6) {
                                            AvatarView(imgPath: user.userImgPath, size: 56)
                                            Text(user.userName)
                                                .font(.caption)
                                                .foregroundColor(.primary)
                                                .lineLimit(1)
                                        }
                                        .frame(width: 80)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                        }
                    }
                }

                // 계정 관리
                HStack(spacing: 12) {
                    Button {
                        appState.isLoggedIn = false
                        appState.username = ""
                        appState.userImgPath = nil
                        UserDefaults.standard.removeObject(forKey: "JWT_TOKEN")
                        UserDefaults.standard.removeObject(forKey: "USERNAME")
                        path.removeLast(path.count)
                    } label: {
                        Text("로그아웃")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    Button {
                        path.append(Page.withdraw)
                    } label: {
                        Text("회원 탈퇴")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding()
        }
        .task {
            await viewModel.load()
        }
        .onChange(of: viewModel.profile?.userImgPath) { newValue in
            appState.userImgPath = newValue
        }
    }
}
