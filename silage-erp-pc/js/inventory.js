const Inventory = {
  CATEGORIES: ['Silage','Wrap','Seeds','Fertilizer','Fuel','Tools','Chemicals','Other'],
  UNITS: ['Tons','Bales','Liters','Kg','Bags','Pieces','Rolls','Other'],

  async render() {
    UI.title('Inventory');
    const rows = await db.getAll('inventory');
    const totalVal = rows.reduce((s,r)=>s+(r.totalValue||0),0);
    const lowStock = rows.filter(r=>r.quantity<=r.minimumStock && r.minimumStock>0);

    const tableRows = rows.length ? rows.map(i => {
      const isLow = i.quantity <= i.minimumStock && i.minimumStock > 0;
      return `<tr style="${isLow?'background:#fff3f3':''}">
        <td>${isLow?'⚠️ ':''}<strong>${i.itemName}</strong></td>
        <td>${i.category||''}</td>
        <td class="${isLow?'text-red':'text-green'} fw-bold">${UI.n(i.quantity,1)} ${i.unit||''}</td>
        <td>${UI.money(i.unitCost)} / ${i.unit||''}</td>
        <td class="fw-bold">${UI.money(i.totalValue)}</td>
        <td>${i.storageLocation||''}</td>
        <td>${i.dateAdded||''}</td>
        ${i.imagePath?`<td><img src="${i.imagePath}" class="photo-thumb"></td>`:'<td>—</td>'}
        <td class="actions">
          <button class="btn btn-sm btn-outline" onclick="Inventory.edit(${i.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Inventory.del(${i.id})">Delete</button>
        </td>
      </tr>`;
    }).join('') : `<tr><td colspan="9">${UI.empty()}</td></tr>`;

    UI.render(`
      ${lowStock.length ? `<div class="alert-low-stock" style="display:block">⚠️ ${lowStock.length} item(s) are below minimum stock level: ${lowStock.map(i=>i.itemName).join(', ')}</div>` : ''}
      <div class="kpi-grid">
        <div class="kpi blue"><div class="kpi-label">Total Items</div><div class="kpi-value">${rows.length}</div></div>
        <div class="kpi green"><div class="kpi-label">Stock Value</div><div class="kpi-value">${UI.money(totalVal)}</div></div>
        ${lowStock.length?`<div class="kpi red"><div class="kpi-label">Low Stock</div><div class="kpi-value">${lowStock.length}</div></div>`:''}
      </div>
      <div class="toolbar"><input type="text" placeholder="Search inventory..." oninput="Inventory.search(this.value)">
        <button class="btn btn-primary" onclick="Inventory.add()">+ Add Item</button></div>
      <div class="card"><div class="table-wrap"><table id="tbl">
        <thead><tr><th>Item</th><th>Category</th><th>Quantity</th><th>Unit Cost</th><th>Total Value</th><th>Storage</th><th>Date Added</th><th>Photo</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },

  async search(q) {
    const rows = await db.getAll('inventory');
    const f = q ? rows.filter(i=>(i.itemName+i.category).toLowerCase().includes(q.toLowerCase())) : rows;
    document.querySelector('#tbl tbody').innerHTML = f.map(i=>`<tr><td>${i.itemName}</td><td>${i.category}</td><td>${UI.n(i.quantity,1)} ${i.unit}</td><td>${UI.money(i.unitCost)}</td><td>${UI.money(i.totalValue)}</td><td>${i.storageLocation||''}</td><td>${i.dateAdded||''}</td><td>—</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="Inventory.edit(${i.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Inventory.del(${i.id})">Delete</button></td></tr>`).join('')||'<tr><td colspan="9" style="text-align:center;color:#999">No results</td></tr>';
  },

  form(i = {}) {
    return `
      <div class="form-row"><div class="form-group"><label>Item Name *</label><input id="itemName" value="${i.itemName||''}"></div>
        <div class="form-group"><label>Category</label><select id="category">${UI.options(this.CATEGORIES,i.category)}</select></div></div>
      <div class="form-row"><div class="form-group"><label>Quantity</label><input id="quantity" type="number" step="0.1" value="${i.quantity||''}"></div>
        <div class="form-group"><label>Unit</label><select id="unit">${UI.options(this.UNITS,i.unit)}</select></div></div>
      <div class="form-row"><div class="form-group"><label>Cost per Unit ($)</label><input id="unitCost" type="number" step="0.01" value="${i.unitCost||''}"></div>
        <div class="form-group"><label>Min Stock Level</label><input id="minimumStock" type="number" step="0.1" value="${i.minimumStock||''}"></div></div>
      <div class="form-row"><div class="form-group"><label>Storage Location</label><input id="storageLocation" value="${i.storageLocation||''}"></div>
        <div class="form-group"><label>Supplier</label><input id="supplierName" value="${i.supplierName||''}"></div></div>
      <div class="form-group"><label>Date Added</label><input id="dateAdded" type="date" value="${i.dateAdded||UI.today()}"></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${i.notes||''}</textarea></div>
      ${UI.photoField('photo', i.imagePath)}`;
  },

  async add() {
    UI.openModal('Add Inventory Item', this.form(), async (body) => {
      const name = UI.val(body,'itemName'); if (!name) { alert('Name required'); return false; }
      const qty = UI.num(body,'quantity'); const cost = UI.num(body,'unitCost');
      const photo = await UI.readPhoto(body.querySelector('#photo'));
      await db.add('inventory', { itemName: name, category: UI.val(body,'category'),
        quantity: qty, unit: UI.val(body,'unit'), unitCost: cost, totalValue: qty*cost,
        storageLocation: UI.val(body,'storageLocation'), supplierName: UI.val(body,'supplierName'),
        dateAdded: UI.val(body,'dateAdded'), minimumStock: UI.num(body,'minimumStock'),
        notes: UI.val(body,'notes'), imagePath: photo });
      this.render();
    });
  },

  async edit(id) {
    const i = await db.get('inventory', id);
    UI.openModal('Edit Inventory Item', this.form(i), async (body) => {
      const qty = UI.num(body,'quantity'); const cost = UI.num(body,'unitCost');
      const photo = await UI.readPhoto(body.querySelector('#photo'));
      await db.put('inventory', { ...i, itemName: UI.val(body,'itemName'), category: UI.val(body,'category'),
        quantity: qty, unit: UI.val(body,'unit'), unitCost: cost, totalValue: qty*cost,
        storageLocation: UI.val(body,'storageLocation'), supplierName: UI.val(body,'supplierName'),
        dateAdded: UI.val(body,'dateAdded'), minimumStock: UI.num(body,'minimumStock'),
        notes: UI.val(body,'notes'), imagePath: photo || i.imagePath });
      this.render();
    });
  },

  async del(id) { if (!UI.confirm('Delete this item?')) return; await db.delete('inventory',id); this.render(); },
};
