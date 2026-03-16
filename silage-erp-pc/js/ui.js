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

  /** Expense types available for additional expenses */
  EXPENSE_TYPES: ['Fuel','Labour','Toll Fees','Permit / Licence','Maintenance','Equipment Hire','Storage','Security','Insurance','Cleaning','Weighing Fee','Other'],

  /** Render the additional expenses section (pre-populated with existing items) */
  additionalExpensesField(list = []) {
    const rows = list.length
      ? list.map(e => this._expenseRow(e)).join('')
      : '';
    return `
      <div class="form-section">📋 Additional Expenses</div>
      <div id="extra-expenses-list">${rows}</div>
      <button type="button" class="btn btn-sm btn-outline" style="margin-bottom:8px" onclick="UI.addExpenseRow()">+ Add Expense</button>`;
  },

  /** Append a new (or pre-filled) expense row to the list */
  addExpenseRow(data = {}) {
    const list = document.getElementById('extra-expenses-list');
    if (!list) return;
    const div = document.createElement('div');
    div.innerHTML = this._expenseRow(data);
    list.appendChild(div.firstElementChild);
  },

  /** Build a single expense row HTML */
  _expenseRow(e = {}) {
    const opts = this.EXPENSE_TYPES.map(t =>
      `<option value="${t}" ${t === e.type ? 'selected' : ''}>${t}</option>`
    ).join('');
    return `<div class="extra-expense-row form-row" style="align-items:center;gap:6px;margin-bottom:6px">
      <div class="form-group" style="flex:1.2;margin:0"><label style="font-size:11px">Type</label>
        <select class="exp-type">${opts}</select></div>
      <div class="form-group" style="flex:0.8;margin:0"><label style="font-size:11px">Amount ($)</label>
        <input class="exp-amount" type="number" step="0.01" placeholder="0.00" value="${e.amount||''}"></div>
      <div class="form-group" style="flex:2;margin:0"><label style="font-size:11px">Remarks</label>
        <input class="exp-remarks" type="text" placeholder="Optional note" value="${e.remarks||''}"></div>
      <div style="padding-top:18px"><button type="button" class="btn btn-sm btn-danger" onclick="this.closest('.extra-expense-row').remove()">✕</button></div>
    </div>`;
  },

  /** Read all additional expense rows from the modal body */
  readAdditionalExpenses(body) {
    const rows = body.querySelectorAll('.extra-expense-row');
    let total = 0;
    const list = [];
    rows.forEach(row => {
      const type    = row.querySelector('.exp-type')?.value || 'Other';
      const amount  = parseFloat(row.querySelector('.exp-amount')?.value) || 0;
      const remarks = row.querySelector('.exp-remarks')?.value?.trim() || '';
      if (amount > 0 || remarks) {
        list.push({ type, amount, remarks });
        total += amount;
      }
    });
    return { list, total };
  },

  /** Shift types for batch/shift tracking */
  SHIFT_TYPES: ['Day Shift','Night Shift','Morning (6am–2pm)','Afternoon (2pm–10pm)','Night (10pm–6am)','Full Day','Custom'],

  /** Render batch/shift tracking section */
  batchShiftField(list = []) {
    const rows = list.length
      ? list.map(b => this._batchRow(b)).join('')
      : '';
    return `
      <div class="form-section">🔄 Batch / Shift Tracking</div>
      <div id="batch-shift-list">${rows}</div>
      <button type="button" class="btn btn-sm btn-outline" style="margin-bottom:8px" onclick="UI.addBatchRow()">+ Add Batch / Shift</button>`;
  },

  /** Append a new (or pre-filled) batch/shift row */
  addBatchRow(data = {}) {
    const list = document.getElementById('batch-shift-list');
    if (!list) return;
    const div = document.createElement('div');
    div.innerHTML = this._batchRow(data);
    list.appendChild(div.firstElementChild);
  },

  /** Build a single batch/shift row HTML */
  _batchRow(b = {}) {
    const shiftOpts = this.SHIFT_TYPES.map(t =>
      `<option value="${t}" ${t === b.shift ? 'selected' : ''}>${t}</option>`
    ).join('');
    const idx = (b.batchNumber !== undefined) ? b.batchNumber : '';
    return `<div class="batch-shift-row" style="background:#f8f9fa;border:1px solid #e0e0e0;border-radius:6px;padding:10px;margin-bottom:8px">
      <div class="form-row" style="align-items:center;gap:6px;margin-bottom:6px">
        <div class="form-group" style="flex:0.5;margin:0"><label style="font-size:11px">Batch #</label>
          <input class="batch-number" type="number" min="1" placeholder="1" value="${idx}" style="text-align:center"></div>
        <div class="form-group" style="flex:1;margin:0"><label style="font-size:11px">Shift</label>
          <select class="batch-shift">${shiftOpts}</select></div>
        <div class="form-group" style="flex:1.2;margin:0"><label style="font-size:11px">Date</label>
          <input class="batch-date" type="date" value="${b.date||''}"></div>
        <div class="form-group" style="flex:1.5;margin:0"><label style="font-size:11px">Operator / Team</label>
          <input class="batch-operator" type="text" placeholder="Name or team" value="${b.operator||''}"></div>
        <div style="padding-top:18px"><button type="button" class="btn btn-sm btn-danger" onclick="this.closest('.batch-shift-row').remove()">✕</button></div>
      </div>
      <div class="form-row" style="gap:6px">
        <div class="form-group" style="flex:1;margin:0"><label style="font-size:11px">Bags / Bales Produced</label>
          <input class="batch-bags" type="number" min="0" placeholder="0" value="${b.bagsProduced||''}"></div>
        <div class="form-group" style="flex:1;margin:0"><label style="font-size:11px">Start Time</label>
          <input class="batch-start" type="time" value="${b.startTime||''}"></div>
        <div class="form-group" style="flex:1;margin:0"><label style="font-size:11px">End Time</label>
          <input class="batch-end" type="time" value="${b.endTime||''}"></div>
        <div class="form-group" style="flex:2;margin:0"><label style="font-size:11px">Remarks</label>
          <input class="batch-remarks" type="text" placeholder="Optional" value="${b.remarks||''}"></div>
      </div>
    </div>`;
  },

  /** Read all batch/shift rows from the modal body */
  readBatchShifts(body) {
    const rows = body.querySelectorAll('.batch-shift-row');
    let totalBags = 0;
    const list = [];
    rows.forEach(row => {
      const bagsProduced = parseInt(row.querySelector('.batch-bags')?.value) || 0;
      list.push({
        batchNumber : parseInt(row.querySelector('.batch-number')?.value) || 0,
        shift       : row.querySelector('.batch-shift')?.value || 'Morning',
        date        : row.querySelector('.batch-date')?.value || '',
        operator    : row.querySelector('.batch-operator')?.value?.trim() || '',
        bagsProduced,
        startTime   : row.querySelector('.batch-start')?.value || '',
        endTime     : row.querySelector('.batch-end')?.value || '',
        remarks     : row.querySelector('.batch-remarks')?.value?.trim() || '',
      });
      totalBags += bagsProduced;
    });
    return { list, totalBags };
  },
};
