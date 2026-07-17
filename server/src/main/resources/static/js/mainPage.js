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

let wheel = document.getElementById("wheel");
let spinning = false;
let currentRotation = 0;

function spin() {
    if (spinning) return;
    spinning = true;

    const count = document.querySelectorAll(".slice").length;
    const sliceDeg = 360 / count;

    // 회전 각도 계산
    const targetIndex = Math.floor(Math.random() * count);
    const targetRotation = 360 * 5 + (360 - sliceDeg * targetIndex - sliceDeg / 2);

    currentRotation = targetRotation;
    wheel.style.transform = `rotate(${currentRotation}deg)`;

    setTimeout(() => {
        showResult(targetIndex);
        spinning = false;
    }, 5000);
}

function showResult(index) {
    const slice = document.querySelectorAll(".slice")[index];
    const title = slice.querySelector(".recipe-title").innerText;
    const img = slice.querySelector("img").src;

    document.getElementById("modalTitle").innerText = title;
    document.getElementById("modalImg").src = img;
    document.getElementById("modal").style.display = "flex";

    document.getElementById("recipeId").value = index;
}

function closeModal() {
    document.getElementById("modal").style.display = "none";
}




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