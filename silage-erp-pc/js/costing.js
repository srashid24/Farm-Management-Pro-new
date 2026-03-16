const Costing = {
  CATEGORIES: ['Seeds','Fertilizer','Labour','Machinery','Fuel','Wrap','Transport','Loading','Unloading','Land Rent','Chemicals','Other'],
  UNITS: ['Kg','Bags','Tons','Liters','Hours','Days','Rolls','Pieces','Ha','Other'],
  async render() {
    UI.title('Costing & Expenses');
    const rows = await db.getAll('costs');
    const totalCost = rows.reduce((s,r)=>s+(r.amount||0),0);
    const byCat = {};
    rows.forEach(r=>{ byCat[r.category]=(byCat[r.category]||0)+(r.amount||0); });
    const tableRows = rows.length ? rows.map(e=>`<tr><td>${UI.badge(e.category,'green')}</td><td>${e.description||''}</td>
      <td>${UI.n(e.quantity,2)} ${e.unit||''}</td><td>${UI.money(e.unitCost)}</td>
      <td class="fw-bold text-red">${UI.money(e.amount)}</td>
      <td>${e.season||''}</td><td>${e.date||''}</td><td>${e.supplierName||''}</td>
      <td class="actions"><button class="btn btn-sm btn-outline" onclick="Costing.edit(${e.id})">Edit</button>
      <button class="btn btn-sm btn-danger" onclick="Costing.del(${e.id})">Delete</button></td></tr>`).join('') : `<tr><td colspan="9">${UI.empty()}</td></tr>`;

    const breakdown = Object.entries(byCat).sort((a,b)=>b[1]-a[1]).map(([cat,amt])=>`<div class="report-row"><span class="label">${cat}</span><span class="value text-red">${UI.money(amt)}</span></div>`).join('');

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi red"><div class="kpi-label">Total Expenses</div><div class="kpi-value">${UI.money(totalCost)}</div></div>
        <div class="kpi"><div class="kpi-label">Cost Entries</div><div class="kpi-value">${rows.length}</div></div>
      </div>
      ${breakdown?`<div class="card"><div class="card-title">Cost Breakdown by Category</div>${breakdown}</div>`:''}
      <div class="toolbar"><button class="btn btn-primary" onclick="Costing.add()">+ Add Expense</button></div>
      <div class="card"><div class="table-wrap"><table id="tbl">
        <thead><tr><th>Category</th><th>Description</th><th>Qty & Unit</th><th>Unit Cost</th><th>Total</th><th>Season</th><th>Date</th><th>Supplier</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },
  form(e={}) {
    return `<div class="form-row">
      <div class="form-group"><label>Category</label><select id="category">${UI.options(this.CATEGORIES,e.category)}</select></div>
      <div class="form-group"><label>Season (e.g. 2025/26)</label><input id="season" value="${e.season||''}"></div></div>
      <div class="form-group"><label>Description *</label><input id="description" value="${e.description||''}"></div>
      <div class="form-row-3">
        <div class="form-group"><label>Quantity</label><input id="quantity" type="number" step="0.01" value="${e.quantity||''}"></div>
        <div class="form-group"><label>Unit</label><select id="unit">${UI.options(this.UNITS,e.unit)}</select></div>
        <div class="form-group"><label>Cost per Unit ($)</label><input id="unitCost" type="number" step="0.01" value="${e.unitCost||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Date</label><input id="date" type="date" value="${e.date||UI.today()}"></div>
        <div class="form-group"><label>Supplier / Vendor</label><input id="supplierName" value="${e.supplierName||''}"></div>
      </div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${e.notes||''}</textarea></div>`;
  },
  async add() { UI.openModal('Add Cost / Expense',this.form(),async(body)=>{ const desc=UI.val(body,'description'); if(!desc){alert('Description required');return false;} const qty=UI.num(body,'quantity'); const uc=UI.num(body,'unitCost'); await db.add('costs',{category:UI.val(body,'category'),description:desc,quantity:qty,unit:UI.val(body,'unit'),unitCost:uc,amount:qty*uc,season:UI.val(body,'season'),date:UI.val(body,'date'),supplierName:UI.val(body,'supplierName'),notes:UI.val(body,'notes')}); this.render(); }); },
  async edit(id) { const e=await db.get('costs',id); UI.openModal('Edit Expense',this.form(e),async(body)=>{ const qty=UI.num(body,'quantity'); const uc=UI.num(body,'unitCost'); await db.put('costs',{...e,category:UI.val(body,'category'),description:UI.val(body,'description'),quantity:qty,unit:UI.val(body,'unit'),unitCost:uc,amount:qty*uc,season:UI.val(body,'season'),date:UI.val(body,'date'),supplierName:UI.val(body,'supplierName'),notes:UI.val(body,'notes')}); this.render(); }); },
  async del(id) { if(!UI.confirm('Delete this expense?'))return; await db.delete('costs',id); this.render(); },
};
