/** UI helper utilities */

const UI = {
  /** Show a modal with a form */
  openModal(title, bodyHtml, onSave) {
    document.getElementById('modal-overlay')?.remove();
    const overlay = document.createElement('div');
    overlay.id = 'modal-overlay';
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
      <div class="modal">
        <div class="modal-header">
          <h3>${title}</h3>
          <button id="modal-close">✕</button>
        </div>
        <div class="modal-body">${bodyHtml}</div>
        <div class="modal-footer">
          <button class="btn btn-secondary" id="modal-cancel">Cancel</button>
          <button class="btn btn-primary" id="modal-save">Save</button>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    const close = () => overlay.remove();
    overlay.querySelector('#modal-close').onclick = close;
    overlay.querySelector('#modal-cancel').onclick = close;
    overlay.onclick = e => { if (e.target === overlay) close(); };
    overlay.querySelector('#modal-save').onclick = async () => {
      const result = await onSave(overlay.querySelector('.modal-body'));
      if (result !== false) close();
    };
  },

  /** Show confirmation dialog */
  confirm(msg) {
    return window.confirm(msg);
  },

  /** Get form field value */
  val(container, id) {
    const el = container.querySelector('#' + id);
    return el ? el.value.trim() : '';
  },

  /** Get numeric form field value */
  num(container, id) {
    const v = parseFloat(this.val(container, id));
    return isNaN(v) ? 0 : v;
  },

  /** Set field value */
  set(container, id, value) {
    const el = container.querySelector('#' + id);
    if (el) el.value = value ?? '';
  },

  /** Format currency */
  money(v) {
    return '$' + (Number(v) || 0).toFixed(2);
  },

  /** Format number */
  n(v, dec = 1) {
    return (Number(v) || 0).toFixed(dec);
  },

  /** Today's date as yyyy-MM-dd */
  today() {
    return new Date().toISOString().slice(0, 10);
  },

  /** Badge HTML */
  badge(text, type = 'grey') {
    return `<span class="badge badge-${type}">${text}</span>`;
  },

  /** Payment status badge */
  payBadge(status) {
    const map = { Paid: 'green', Partial: 'orange', Unpaid: 'red' };
    return this.badge(status, map[status] || 'grey');
  },

  /** Delivery status badge */
  deliveryBadge(status) {
    const map = { Delivered: 'green', 'In Transit': 'blue', Pending: 'orange', Cancelled: 'red' };
    return this.badge(status, map[status] || 'grey');
  },

  /** Render empty state */
  empty(msg = 'No records yet. Click + Add to get started.') {
    return `<div class="empty-state"><div class="icon">📋</div><p>${msg}</p></div>`;
  },

  /** Render content area */
  render(html) {
    document.getElementById('content').innerHTML = html;
  },

  /** Set page title */
  title(t) {
    document.getElementById('page-title').textContent = t;
  },

  /** Generate invoice number */
  invoiceNum() {
    return 'INV-' + Date.now().toString().slice(-8);
  },

  /** Build select options from array */
  options(arr, selected = '') {
    return arr.map(v => `<option value="${v}" ${v === selected ? 'selected' : ''}>${v}</option>`).join('');
  },

  /** Build select options from objects */
  objOptions(arr, valueKey, labelKey, selected = '') {
    return arr.map(o => `<option value="${o[valueKey]}" ${o[valueKey] == selected ? 'selected' : ''}>${o[labelKey]}</option>`).join('');
  },

  /** Photo preview helper */
  photoField(id, currentSrc = '') {
    return `
      <div class="form-group">
        <label>Photo (optional)</label>
        ${currentSrc ? `<img src="${currentSrc}" style="width:80px;height:80px;object-fit:cover;border-radius:6px;margin-bottom:6px;display:block">` : ''}
        <input type="file" id="${id}" accept="image/*" style="display:block">
      </div>`;
  },

  /** Read file as base64 data URL */
  readPhoto(input) {
    return new Promise((resolve) => {
      if (!input || !input.files || !input.files[0]) { resolve(null); return; }
      const reader = new FileReader();
      reader.onload = e => resolve(e.target.result);
      reader.readAsDataURL(input.files[0]);
    });
  },
};
