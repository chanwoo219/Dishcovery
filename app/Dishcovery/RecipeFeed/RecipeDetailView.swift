import SwiftUI

struct RecipeStep: Codable {
    let stepOrder: Int?
    let stepDescription: String?
}

struct RecipeDetailView: View {
    let recipe: Recipe
    @Binding var path: NavigationPath

    @EnvironmentObject var appState: AppState

    @State private var steps: [RecipeStep] = []
    @State private var comments: [RecipeComment] = []
    @State private var likeStatus = RecipeLikeStatus(likeCount: 0, liked: false)
    @State private var newCommentText: String = ""
    @State private var errorMessage: String?
    @State private var pendingDeleteComment: RecipeComment?
    @State private var showDeleteRecipeConfirm = false

    private var isMine: Bool {
        !appState.userId.isEmpty && appState.userId == recipe.userId
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {

                // 이미지

                if let path = recipe.imgUrl {
                    Rectangle()
                        .fill(Color(.secondarySystemBackground))
                        .frame(height: 250)
                        .frame(maxWidth: .infinity)
                        .overlay(
                            AsyncImage(
                                url: URL(string: (API.baseURL) + path)
                            ) { image in
                                image.resizable().scaledToFill()
                            } placeholder: {
                                Color.clear
                            }
                        )
                        .clipped()
                        .cornerRadius(12)
                }


                // 제목
                Text(recipe.title)
                    .font(.title)
                    .fontWeight(.bold)

                // 짧은 설명
                if let desc = recipe.rcpDisc {
                    Text(desc)
                        .font(.body)
                        .foregroundColor(.gray)
                }

                // 조리시간 / 난이도 / 해시태그
                HStack(spacing: 16) {
                    Label(recipe.cookTime, systemImage: "clock")
                    Label(recipe.cookDfct, systemImage: "gauge")
                }
                .font(.subheadline)
                .foregroundColor(.secondary)

                if let tag = recipe.recipeTag, !tag.isEmpty {
                    Text(tag)
                        .font(.subheadline)
                        .foregroundColor(.blue)
                }

                Divider()

                // 재료
                VStack(alignment: .leading, spacing: 8) {
                    Text("🧂 재료")
                        .font(.headline)
                    Text(recipe.recipeIngr)
                        .font(.body)
                }

                Divider()

                // 조리 방법
                VStack(alignment: .leading, spacing: 8) {
                    Text("🍳 조리 tip")
                        .font(.headline)
                    Text(recipe.recipeTip)
                        .font(.body)
                }
                Divider()

                // 조리 단계
                if !steps.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("🍽 조리 순서")
                            .font(.headline)

                        ForEach(steps.indices, id: \.self) { index in
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Step \(index + 1)")
                                    .font(.subheadline)
                                    .fontWeight(.bold)

                                Text(steps[index].stepDescription ?? "")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(12)
                        }
                    }

                    Divider()
                }

                // 좋아요
                Button {
                    Task { await toggleLike() }
                } label: {
                    Label("좋아요 \(likeStatus.likeCount)", systemImage: likeStatus.liked ? "heart.fill" : "heart")
                        .foregroundColor(likeStatus.liked ? .red : .secondary)
                }

                // 수정/삭제 (내 레시피일 때만)
                if isMine {
                    HStack(spacing: 12) {
                        Button {
                            path.append(Page.recipeEdit(recipeId: recipe.recipeId))
                        } label: {
                            Text("수정")
                                .font(.subheadline)
                        }

                        Button(role: .destructive) {
                            showDeleteRecipeConfirm = true
                        } label: {
                            Text("삭제")
                                .font(.subheadline)
                        }
                    }
                }

                Divider()

                // 댓글
                VStack(alignment: .leading, spacing: 10) {
                    Text("💬 댓글 \(comments.count)개")
                        .font(.headline)

                    HStack {
                        TextField("댓글을 입력해주세요", text: $newCommentText)
                            .textFieldStyle(.roundedBorder)

                        Button("등록") {
                            Task { await submitComment() }
                        }
                        .disabled(newCommentText.trimmingCharacters(in: .whitespaces).isEmpty)
                    }

                    ForEach(comments) { comment in
                        HStack(alignment: .top) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(comment.userName ?? comment.userId)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text(comment.content)
                                    .font(.body)
                            }
                            Spacer()
                            if comment.userName == appState.username {
                                Button {
                                    pendingDeleteComment = comment
                                } label: {
                                    Image(systemName: "trash")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }

                Spacer()
            }
            .padding()
        }
        .navigationTitle("레시피 상세")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadSteps()
            await loadComments()
            await loadLikeStatus()
        }
        .alert("알림", isPresented: Binding(
            get: { errorMessage != nil },
            set: { if !$0 { errorMessage = nil } }
        )) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "")
        }
        .alert("댓글 삭제", isPresented: Binding(
            get: { pendingDeleteComment != nil },
            set: { if !$0 { pendingDeleteComment = nil } }
        )) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                if let comment = pendingDeleteComment {
                    Task { await removeComment(comment) }
                }
            }
        } message: {
            Text("정말로 삭제하시겠습니까?")
        }
        .alert("레시피 삭제", isPresented: $showDeleteRecipeConfirm) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                Task { await deleteRecipe() }
            }
        } message: {
            Text("정말로 삭제하시겠습니까?")
        }
    }

    private func loadSteps() async {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipe.recipeId)/steps") else { return }
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            let decoded = try JSONDecoder().decode([RecipeStep].self, from: data)
            steps = decoded.sorted { ($0.stepOrder ?? 0) < ($1.stepOrder ?? 0) }
        } catch {
            print("🔴 조리 단계 불러오기 실패:", error.localizedDescription)
        }
    }

    private func loadComments() async {
        do {
            comments = try await RecipeApiService.shared.fetchComments(recipeId: recipe.recipeId)
        } catch {
            comments = []
        }
    }

    private func loadLikeStatus() async {
        do {
            likeStatus = try await RecipeApiService.shared.fetchLikeStatus(recipeId: recipe.recipeId)
        } catch {
            likeStatus = RecipeLikeStatus(likeCount: 0, liked: false)
        }
    }

    private func toggleLike() async {
        do {
            likeStatus = try await RecipeApiService.shared.toggleLike(recipeId: recipe.recipeId)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func submitComment() async {
        do {
            try await RecipeApiService.shared.addComment(recipeId: recipe.recipeId, content: newCommentText)
            newCommentText = ""
            errorMessage = nil
            await loadComments()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func deleteRecipe() async {
        do {
            try await RecipeApiService.shared.deleteRecipe(recipeId: recipe.recipeId)
            path.removeLast()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func removeComment(_ comment: RecipeComment) async {
        pendingDeleteComment = nil
        do {
            try await RecipeApiService.shared.deleteComment(recipeId: recipe.recipeId, commentId: comment.commentId)
            errorMessage = nil
            await loadComments()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
