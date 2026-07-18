// Sort UI toggle (design only)
document.addEventListener('click', (e) => {
    const sortBtn = e.target.closest('.sort');
    if (!sortBtn) return;

    const wrap = sortBtn.parentElement;
    wrap.querySelectorAll('.sort').forEach(b => b.classList.remove('active'));
    sortBtn.classList.add('active');

    // 서버 정렬로 재조회 (MainPage / searchRecipe 둘 다 지원)
    const sort = sortBtn.dataset.sort;
    const url = new URL(window.location.href);
    url.searchParams.set('sort', sort);

    // searchRecipe 페이지에서는 searchName이 필수라 form action 유지
    window.location.href = url.toString();
});

function selectDetail(recipeId) {
    const form = document.createElement("form");
    form.method = "GET";
    form.action = "/recipe/detail";
    form.target = "_self";

    const hidden = document.createElement("input");
    hidden.type = "hidden";
    hidden.name = "recipeId";
    hidden.value = recipeId;

    form.appendChild(hidden);
    document.body.appendChild(form);
    form.submit();
}

function goToShop(button) {
    var loggedIn = document.querySelector('.topbar')?.dataset.loggedIn === 'true';
    if (!loggedIn) {
        alert('로그인이 필요한 서비스입니다.');
        return;
    }
    handleSegmentClick(button, '/pageGubun?gubun=rank');
}

function handleSegmentClick(button, url) {
    // 활성화 클래스 토글
    const wrap = button.parentElement;
    wrap.querySelectorAll('button').forEach(b => {
        b.classList.remove('active');
        b.setAttribute('aria-selected', 'false');
    });
    button.classList.add('active');
    button.setAttribute('aria-selected', 'true');

    setTimeout(() => {
        window.location.href = url;
    }, 300);

}

function confirmLogout(){
    if(confirm("정말 로그아웃 하시겠습니까 ?")){
        window.location.href = "/logout";
    }
}