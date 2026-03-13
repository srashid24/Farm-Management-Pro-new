/** Farmers Module */
const Farmers = {
  async render() {
    UI.title('Farmers');
    const rows = await db.getAll('farmers');
    const tableRows = rows.length ? rows.map(f => `
      <tr>
        <td>${f.name}</td>
        <td>${f.phone || ''}</td>
        <td>${f.address || ''}</td>
        <td>${f.nationalId || ''}</td>
        <td>${f.email || ''}</td>
        <td>${f.notes || ''}</td>
        <td class="actions">
          <button class="btn btn-sm btn-outline" onclick="Farmers.edit(${f.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Farmers.del(${f.id}, '${f.name.replace(/'/g,"\\'")}')">Delete</button>
        </td>
      </tr>`).join('') : `<tr><td colspan="7">${UI.empty()}</td></tr>`;

    UI.render(`
      <div class="toolbar">
        <input type="text" id="search" placeholder="Search farmers..." oninput="Farmers.search(this.value)">
        <button class="btn btn-primary" onclick="Farmers.add()">+ Add Farmer</button>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table id="tbl">
            <thead><tr><th>Name</th><th>Phone</th><th>Address</th><th>National ID</th><th>Email</th><th>Notes</th><th>Actions</th></tr></thead>
            <tbody>${tableRows}</tbody>
          </table>
        </div>
      </div>`);
  },

  async search(q) {
    const rows = await db.getAll('farmers');
    const filtered = q ? rows.filter(f => (f.name+f.phone+f.address).toLowerCase().includes(q.toLowerCase())) : rows;
    document.querySelector('#tbl tbody').innerHTML = filtered.length
      ? filtered.map(f => `<tr><td>${f.name}</td><td>${f.phone||''}</td><td>${f.address||''}</td><td>${f.nationalId||''}</td><td>${f.email||''}</td><td>${f.notes||''}</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="Farmers.edit(${f.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Farmers.del(${f.id}, '${f.name.replace(/'/g,"\\'")}')">Delete</button></td></tr>`).join('')
      : `<tr><td colspan="7" style="text-align:center;color:#999">No results</td></tr>`;
  },

  form(f = {}) {
    return `
      <div class="form-row">
        <div class="form-group"><label>Full Name *</label><input id="name" value="${f.name||''}"></div>
        <div class="form-group"><label>Phone</label><input id="phone" value="${f.phone||''}"></div>
      </div>
      <div class="form-group"><label>Address / Town</label><input id="address" value="${f.address||''}"></div>
      <div class="form-row">
        <div class="form-group"><label>National ID / Reg No</label><input id="nationalId" value="${f.nationalId||''}"></div>
        <div class="form-group"><label>Email</label><input id="email" type="email" value="${f.email||''}"></div>
      </div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${f.notes||''}</textarea></div>`;
  },

  add() {
    UI.openModal('Add Farmer', this.form(), async (body) => {
      const name = UI.val(body,'name');
      if (!name) { alert('Name is required'); return false; }
      await db.add('farmers', { name, phone: UI.val(body,'phone'), address: UI.val(body,'address'), nationalId: UI.val(body,'nationalId'), email: UI.val(body,'email'), notes: UI.val(body,'notes') });
      this.render();
    });
  },

  async edit(id) {
    const f = await db.get('farmers', id);
    UI.openModal('Edit Farmer', this.form(f), async (body) => {
      const name = UI.val(body,'name');
      if (!name) { alert('Name is required'); return false; }
      await db.put('farmers', { ...f, name, phone: UI.val(body,'phone'), address: UI.val(body,'address'), nationalId: UI.val(body,'nationalId'), email: UI.val(body,'email'), notes: UI.val(body,'notes') });
      this.render();
    });
  },

  async del(id, name) {
    if (!UI.confirm(`Delete farmer "${name}"? All linked lands will also be deleted.`)) return;
    await db.delete('farmers', id);
    this.render();
  },
};
