const createStepRow = (index) => {
    const row = document.createElement('div');
    row.className = 'step-row';
    row.dataset.step = '';
    row.innerHTML = `
    <span class="step-index">${index}</span>
    <textarea name="stepDescriptions" rows="2" placeholder="예: 팬에 올리브유를 두르고 다진 마늘을 볶습니다." required></textarea>
    <button type="button" class="step-remove" data-remove-step aria-label="${index}단계 삭제">&times;</button>
  `;
    return row;
};

const updateStepIndexes = (list) => {
    list.querySelectorAll('[data-step]').forEach((row, idx) => {
        const index = idx + 1;
        row.querySelector('.step-index').textContent = index;
        row.querySelector('[data-remove-step]').setAttribute('aria-label', `${index}단계 삭제`);
    });
};

const ensureStepList = (list) => {
    if (!list) return;
    if (!list.querySelector('[data-step]')) {
        list.appendChild(createStepRow(1));
    }
    updateStepIndexes(list);
};

const showToast = (message) => {
    const toast = document.querySelector('.toast');
    if (!toast) return;
    toast.textContent = message;
    toast.hidden = false;
    requestAnimationFrame(() => toast.classList.add('show'));
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => (toast.hidden = true), 300);
    }, 2500);
};

document.addEventListener('DOMContentLoaded', () => {
    const stepList = document.querySelector('[data-step-list]');
    ensureStepList(stepList);

    document.addEventListener('click', (e) => {
        if (e.target.closest('[data-add-step]')) {
            const list = document.querySelector('[data-step-list]');
            if (!list) return;
            const newRow = createStepRow(list.querySelectorAll('[data-step]').length + 1);
            list.appendChild(newRow);
            updateStepIndexes(list);
            newRow.querySelector('textarea')?.focus();
        }

        if (e.target.closest('[data-remove-step]')) {
            const row = e.target.closest('[data-step]');
            const list = document.querySelector('[data-step-list]');
            if (!row || !list) return;
            row.remove();
            ensureStepList(list);
        }
    });

});

function previewSingleImage(event) {
    const previewArea = document.getElementById('mainPreviewArea');
    const file = event.target.files[0]; // 첫 번째 파일만 가져오기
    previewArea.innerHTML = ''; // 이전 미리보기 삭제

    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.alt = '대표 이미지 미리보기';
            img.style.width = '150px';
            img.style.height = '150px';
            img.style.objectFit = 'cover';
            img.style.borderRadius = '10px';
            previewArea.appendChild(img);
        };
        reader.readAsDataURL(file);
    } else {
        previewArea.innerHTML = '<span>미리보기</span>';
    }
}