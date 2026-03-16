/** Customer Management Module */
const Buyers = {
  TYPES: ['Individual','Farm','Retail','Wholesaler','Feedlot','Dairy','Restaurant','Processing Plant','Game Farm','Co-operative','Other'],
  PAYMENT_TERMS: ['Cash','Net 7','Net 15','Net 30','Net 60','Net 90'],

  async render() {
    UI.title('Customers');
    const rows = await db.getAll('buyers');
    const allSales = await db.getAll('sales');

    // Recalculate totals from actual sales for accuracy
    for (const b of rows) {
      const bSales = allSales.filter(s => s.buyerId === b.id);
      b.totalPurchased = bSales.reduce((s,r) => s + (r.totalAmount||0), 0);
      b.totalOwed = bSales.reduce((s,r) => s + (r.balance||0), 0);
    }

    const totalOwed = rows.reduce((s,r) => s + (r.totalOwed||0), 0);
    const withDues  = rows.filter(b => b.totalOwed > 0).length;
    const types     = [...new Set(rows.map(b => b.buyerType).filter(Boolean))];

    const tableRows = rows.length ? rows.map(b => `
      <tr>
        <td><strong>${b.name}</strong>${b.company ? `<br><small style="color:#757575">${b.company}</small>` : ''}</td>
        <td>${b.phone||''}</td>
        <td>${b.buyerType ? `<span class="badge badge-blue">${b.buyerType}</span>` : ''}</td>
        <td><small>${b.region||b.address||'—'}</small>${b.gpsLocation ? `<br><small style="color:#1565C0">📍 ${b.gpsLocation}</small>` : ''}</td>
        <td>${b.paymentTerms||'Cash'}</td>
        <td>${b.creditLimit ? UI.money(b.creditLimit) : '—'}</td>
        <td class="${b.totalOwed > 0 ? 'text-red fw-bold' : ''}">${b.totalOwed > 0 ? UI.money(b.totalOwed) : '—'}</td>
        <td class="actions">
          <button class="btn btn-sm btn-blue"    onclick="Buyers.view(${b.id})">View</button>
          <button class="btn btn-sm btn-outline" onclick="Buyers.edit(${b.id})">Edit</button>
          <button class="btn btn-sm btn-danger"  onclick="Buyers.del(${b.id},'${b.name.replace(/'/g,"\\'").replace(/"/g,'&quot;')}')">Delete</button>
        </td>
      </tr>`).join('') : `<tr><td colspan="8">${UI.empty('No customers yet. Click + Add Customer to get started.')}</td></tr>`;

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Customers</div><div class="kpi-value">${rows.length}</div></div>
        ${totalOwed > 0 ? `<div class="kpi red"><div class="kpi-label">Total Outstanding</div><div class="kpi-value">${UI.money(totalOwed)}</div></div>` : ''}
        ${withDues  > 0 ? `<div class="kpi orange"><div class="kpi-label">Customers with Dues</div><div class="kpi-value">${withDues}</div></div>` : ''}
      </div>
      <div class="toolbar">
        <input type="text" id="search-input" placeholder="Search customers..." oninput="Buyers.search(this.value)">
        <select id="type-filter" onchange="Buyers.filterType(this.value)" style="padding:8px 10px;border:1px solid #E0E0E0;border-radius:6px;font-size:13px">
          <option value="">All Types</option>
          ${this.TYPES.map(t => `<option value="${t}">${t}</option>`).join('')}
        </select>
        <button class="btn btn-primary" onclick="Buyers.add()">+ Add Customer</button>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table id="tbl">
            <thead><tr><th>Name</th><th>Phone</th><th>Type</th><th>Location</th><th>Terms</th><th>Credit Limit</th><th>Outstanding</th><th>Actions</th></tr></thead>
            <tbody>${tableRows}</tbody>
          </table>
        </div>
      </div>`);
  },

  async search(q) {
    const typeFilter = document.querySelector('#type-filter')?.value || '';
    const rows = await db.getAll('buyers');
    const allSales = await db.getAll('sales');
    for (const b of rows) {
      b.totalOwed = allSales.filter(s => s.buyerId === b.id).reduce((s,r) => s + (r.balance||0), 0);
    }
    let f = q ? rows.filter(b => (b.name+b.company+b.phone+b.region+b.address).toLowerCase().includes(q.toLowerCase())) : rows;
    if (typeFilter) f = f.filter(b => b.buyerType === typeFilter);
    this._updateTable(f);
  },

  async filterType(type) {
    const q = document.querySelector('#search-input')?.value || '';
    const rows = await db.getAll('buyers');
    const allSales = await db.getAll('sales');
    for (const b of rows) {
      b.totalOwed = allSales.filter(s => s.buyerId === b.id).reduce((s,r) => s + (r.balance||0), 0);
    }
    let f = type ? rows.filter(b => b.buyerType === type) : rows;
    if (q) f = f.filter(b => (b.name+b.company+b.phone+b.region+b.address).toLowerCase().includes(q.toLowerCase()));
    this._updateTable(f);
  },

  _updateTable(rows) {
    document.querySelector('#tbl tbody').innerHTML = rows.map(b => `
      <tr>
        <td><strong>${b.name}</strong>${b.company ? `<br><small style="color:#757575">${b.company}</small>` : ''}</td>
        <td>${b.phone||''}</td>
        <td>${b.buyerType ? `<span class="badge badge-blue">${b.buyerType}</span>` : ''}</td>
        <td><small>${b.region||b.address||'—'}</small></td>
        <td>${b.paymentTerms||'Cash'}</td>
        <td>${b.creditLimit ? UI.money(b.creditLimit) : '—'}</td>
        <td class="${b.totalOwed > 0 ? 'text-red fw-bold' : ''}">${b.totalOwed > 0 ? UI.money(b.totalOwed) : '—'}</td>
        <td class="actions">
          <button class="btn btn-sm btn-blue"    onclick="Buyers.view(${b.id})">View</button>
          <button class="btn btn-sm btn-outline" onclick="Buyers.edit(${b.id})">Edit</button>
          <button class="btn btn-sm btn-danger"  onclick="Buyers.del(${b.id},'${b.name.replace(/'/g,"\\'")}')">Delete</button>
        </td>
      </tr>`).join('') || `<tr><td colspan="8" style="text-align:center;color:#999">No results</td></tr>`;
  },

  form(b = {}) {
    return `
      <div class="form-row">
        <div class="form-group"><label>Full Name *</label><input id="name" value="${b.name||''}"></div>
        <div class="form-group"><label>Phone</label><input id="phone" value="${b.phone||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Company / Farm Name</label><input id="company" value="${b.company||''}"></div>
        <div class="form-group"><label>Email</label><input id="email" type="email" value="${b.email||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Customer Type</label><select id="buyerType">${UI.options(this.TYPES, b.buyerType)}</select></div>
        <div class="form-group"><label>Tax / VAT Number</label><input id="taxNumber" placeholder="Optional" value="${b.taxNumber||''}"></div>
      </div>
      <div class="form-section">📍 Location</div>
      <div class="form-group"><label>Street Address</label><input id="address" value="${b.address||''}"></div>
      <div class="form-row">
        <div class="form-group"><label>Region / Province</label><input id="region" value="${b.region||''}"></div>
        <div class="form-group"><label>GPS Coordinates</label><input id="gpsLocation" placeholder="e.g. -25.7461, 28.1881" value="${b.gpsLocation||''}"></div>
      </div>
      <div class="form-section">💳 Accounting</div>
      <div class="form-row">
        <div class="form-group"><label>Payment Terms</label><select id="paymentTerms">${UI.options(this.PAYMENT_TERMS, b.paymentTerms||'Cash')}</select></div>
        <div class="form-group"><label>Credit Limit ($)</label><input id="creditLimit" type="number" step="0.01" placeholder="0 = no limit" value="${b.creditLimit||''}"></div>
      </div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${b.notes||''}</textarea></div>`;
  },

  async add() {
    UI.openModal('Add Customer', this.form(), async (body) => {
      const name = UI.val(body, 'name');
      if (!name) { alert('Name required'); return false; }
      await db.add('buyers', {
        name, phone: UI.val(body,'phone'), company: UI.val(body,'company'),
        email: UI.val(body,'email'), address: UI.val(body,'address'),
        buyerType: UI.val(body,'buyerType'), taxNumber: UI.val(body,'taxNumber'),
        region: UI.val(body,'region'), gpsLocation: UI.val(body,'gpsLocation'),
        paymentTerms: UI.val(body,'paymentTerms') || 'Cash',
        creditLimit: UI.num(body,'creditLimit'),
        notes: UI.val(body,'notes'), totalPurchased: 0, totalOwed: 0,
      });
      this.render();
    });
  },

  async edit(id) {
    const b = await db.get('buyers', id);
    UI.openModal('Edit Customer', this.form(b), async (body) => {
      await db.put('buyers', {
        ...b,
        name: UI.val(body,'name'), phone: UI.val(body,'phone'),
        company: UI.val(body,'company'), email: UI.val(body,'email'),
        address: UI.val(body,'address'), buyerType: UI.val(body,'buyerType'),
        taxNumber: UI.val(body,'taxNumber'), region: UI.val(body,'region'),
        gpsLocation: UI.val(body,'gpsLocation'),
        paymentTerms: UI.val(body,'paymentTerms'),
        creditLimit: UI.num(body,'creditLimit'),
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async view(id) {
    const b = await db.get('buyers', id);
    const allSales = await db.getAll('sales');
    const bSales = allSales
      .filter(s => s.buyerId === id)
      .sort((a, z) => new Date(z.saleDate) - new Date(a.saleDate));

    const totalPurchased = bSales.reduce((s,r) => s + (r.totalAmount||0), 0);
    const totalPaid      = bSales.reduce((s,r) => s + (r.amountPaid||0), 0);
    const totalOwed      = bSales.reduce((s,r) => s + (r.balance||0), 0);

    // Aging analysis on outstanding balances
    const now = Date.now();
    const aging = { d30: 0, d60: 0, d90: 0, dOld: 0 };
    bSales.filter(s => s.balance > 0).forEach(s => {
      const days = Math.floor((now - new Date(s.saleDate).getTime()) / 86400000);
      if      (days <= 30) aging.d30  += s.balance;
      else if (days <= 60) aging.d60  += s.balance;
      else if (days <= 90) aging.d90  += s.balance;
      else                 aging.dOld += s.balance;
    });

    const creditAvail = b.creditLimit ? Math.max(0, b.creditLimit - totalOwed) : null;

    const infoGrid = `
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px 20px;margin-bottom:16px;font-size:13px">
        <div><span style="color:#757575">Type:</span> <strong>${b.buyerType||'—'}</strong></div>
        <div><span style="color:#757575">Phone:</span> <strong>${b.phone||'—'}</strong></div>
        <div><span style="color:#757575">Company:</span> <strong>${b.company||'—'}</strong></div>
        <div><span style="color:#757575">Email:</span> <strong>${b.email||'—'}</strong></div>
        <div><span style="color:#757575">Address:</span> <strong>${b.address||'—'}</strong></div>
        <div><span style="color:#757575">Region:</span> <strong>${b.region||'—'}</strong></div>
        ${b.gpsLocation ? `<div><span style="color:#757575">GPS:</span> <strong>${b.gpsLocation}</strong></div>` : ''}
        ${b.taxNumber   ? `<div><span style="color:#757575">Tax/VAT:</span> <strong>${b.taxNumber}</strong></div>` : ''}
        <div><span style="color:#757575">Payment Terms:</span> <strong>${b.paymentTerms||'Cash'}</strong></div>
        ${b.creditLimit ? `<div><span style="color:#757575">Credit Limit:</span> <strong>${UI.money(b.creditLimit)}</strong></div>` : ''}
      </div>`;

    const kpis = `
      <div class="kpi-grid" style="margin-bottom:16px">
        <div class="kpi green"  style="padding:12px"><div class="kpi-label">Total Purchased</div><div class="kpi-value" style="font-size:1.3em">${UI.money(totalPurchased)}</div></div>
        <div class="kpi blue"   style="padding:12px"><div class="kpi-label">Total Paid</div><div class="kpi-value" style="font-size:1.3em">${UI.money(totalPaid)}</div></div>
        ${totalOwed > 0 ? `<div class="kpi red" style="padding:12px"><div class="kpi-label">Outstanding</div><div class="kpi-value" style="font-size:1.3em">${UI.money(totalOwed)}</div></div>` : ''}
        ${creditAvail !== null ? `<div class="kpi" style="padding:12px"><div class="kpi-label">Credit Available</div><div class="kpi-value" style="font-size:1.3em;color:${creditAvail>0?'#1B5E20':'#C62828'}">${UI.money(creditAvail)}</div></div>` : ''}
      </div>`;

    const agingTable = totalOwed > 0 ? `
      <div class="form-section">Aging Analysis</div>
      <table style="width:100%;border-collapse:collapse;margin-bottom:16px;font-size:13px">
        <thead><tr style="background:#f5f5f5"><th style="padding:7px 10px;text-align:left;font-weight:600">Period</th><th style="padding:7px 10px;text-align:right;font-weight:600">Outstanding</th></tr></thead>
        <tbody>
          <tr style="border-bottom:1px solid #eee"><td style="padding:7px 10px">0 – 30 days</td>     <td style="padding:7px 10px;text-align:right;font-weight:${aging.d30>0?'700':'400'};color:${aging.d30>0?'#E65100':'#2E7D32'}">${UI.money(aging.d30)}</td></tr>
          <tr style="border-bottom:1px solid #eee"><td style="padding:7px 10px">31 – 60 days</td>    <td style="padding:7px 10px;text-align:right;font-weight:${aging.d60>0?'700':'400'};color:${aging.d60>0?'#C62828':'#2E7D32'}">${UI.money(aging.d60)}</td></tr>
          <tr style="border-bottom:1px solid #eee"><td style="padding:7px 10px">61 – 90 days</td>    <td style="padding:7px 10px;text-align:right;font-weight:${aging.d90>0?'700':'400'};color:${aging.d90>0?'#C62828':'#2E7D32'}">${UI.money(aging.d90)}</td></tr>
          <tr>                                      <td style="padding:7px 10px">&gt; 90 days</td>    <td style="padding:7px 10px;text-align:right;font-weight:${aging.dOld>0?'700':'400'};color:${aging.dOld>0?'#B71C1C':'#2E7D32'}">${UI.money(aging.dOld)}</td></tr>
        </tbody>
      </table>` : '';

    const salesRows = bSales.length ? bSales.map(s => `
      <tr>
        <td><strong>${s.invoiceNumber||''}</strong></td>
        <td>${s.saleDate||''}</td>
        <td>${s.silagType||''}</td>
        <td>${UI.n(s.quantityTons,1)}t / ${s.balesCount||0} bales</td>
        <td class="fw-bold">${UI.money(s.totalAmount)}</td>
        <td>${UI.money(s.amountPaid)}</td>
        <td class="${s.balance>0?'text-red fw-bold':''}">${UI.money(s.balance)}</td>
        <td>${UI.deliveryBadge(s.deliveryStatus)}</td>
        <td>${UI.payBadge(s.paymentStatus)}</td>
      </tr>`).join('') : `<tr><td colspan="9" style="text-align:center;color:#999;padding:20px">No transactions yet</td></tr>`;

    UI.openModal(`Customer: ${b.name}`, `
      ${infoGrid}
      ${kpis}
      ${agingTable}
      <div class="form-section">Transaction History (${bSales.length} record${bSales.length===1?'':'s'})</div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Invoice</th><th>Date</th><th>Type</th><th>Quantity</th><th>Total</th><th>Paid</th><th>Balance</th><th>Delivery</th><th>Payment</th></tr></thead>
          <tbody>${salesRows}</tbody>
        </table>
      </div>
      ${b.notes ? `<div style="margin-top:14px;padding:10px;background:#f9f9f9;border-radius:6px;font-size:13px"><strong>Notes:</strong> ${b.notes}</div>` : ''}
    `, () => true);
  },

  async del(id, name) {
    if (!UI.confirm(`Delete customer "${name}"?`)) return;
    await db.delete('buyers', id);
    this.render();
  },
};
