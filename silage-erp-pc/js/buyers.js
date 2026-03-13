const Buyers = {
  TYPES: ['Individual','Farm','Feedlot','Dairy','Game Farm','Other'],
  async render() {
    UI.title('Buyers');
    const rows = await db.getAll('buyers');
    const totalOwed = rows.reduce((s,r)=>s+(r.totalOwed||0),0);
    const tableRows = rows.length ? rows.map(b=>`<tr><td><strong>${b.name}</strong></td><td>${b.phone||''}</td><td>${b.company||''}</td><td>${b.address||''}</td><td>${b.buyerType||''}</td><td class="${b.totalOwed>0?'text-red fw-bold':''}">${b.totalOwed?UI.money(b.totalOwed):''}</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="Buyers.edit(${b.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Buyers.del(${b.id},'${b.name.replace(/'/g,"\\'")}')">Delete</button></td></tr>`).join('') : `<tr><td colspan="7">${UI.empty()}</td></tr>`;
    UI.render(`
      <div class="kpi-grid"><div class="kpi green"><div class="kpi-label">Total Buyers</div><div class="kpi-value">${rows.length}</div></div>
        ${totalOwed>0?`<div class="kpi red"><div class="kpi-label">Total Owed</div><div class="kpi-value">${UI.money(totalOwed)}</div></div>`:''}</div>
      <div class="toolbar"><input type="text" placeholder="Search buyers..." oninput="Buyers.search(this.value)">
        <button class="btn btn-primary" onclick="Buyers.add()">+ Add Buyer</button></div>
      <div class="card"><div class="table-wrap"><table id="tbl">
        <thead><tr><th>Name</th><th>Phone</th><th>Company</th><th>Address</th><th>Type</th><th>Outstanding</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },
  async search(q) {
    const rows = await db.getAll('buyers');
    const f = q ? rows.filter(b=>(b.name+b.company+b.phone).toLowerCase().includes(q.toLowerCase())) : rows;
    document.querySelector('#tbl tbody').innerHTML = f.map(b=>`<tr><td>${b.name}</td><td>${b.phone||''}</td><td>${b.company||''}</td><td>${b.address||''}</td><td>${b.buyerType||''}</td><td>${b.totalOwed?UI.money(b.totalOwed):''}</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="Buyers.edit(${b.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Buyers.del(${b.id},'${b.name.replace(/'/g,"\\'")}')">Delete</button></td></tr>`).join('')||'<tr><td colspan="7" style="text-align:center;color:#999">No results</td></tr>';
  },
  form(b={}) {
    return `<div class="form-row"><div class="form-group"><label>Full Name *</label><input id="name" value="${b.name||''}"></div>
      <div class="form-group"><label>Phone</label><input id="phone" value="${b.phone||''}"></div></div>
      <div class="form-row"><div class="form-group"><label>Company / Farm</label><input id="company" value="${b.company||''}"></div>
      <div class="form-group"><label>Email</label><input id="email" type="email" value="${b.email||''}"></div></div>
      <div class="form-group"><label>Address</label><input id="address" value="${b.address||''}"></div>
      <div class="form-group"><label>Buyer Type</label><select id="buyerType">${UI.options(this.TYPES,b.buyerType)}</select></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${b.notes||''}</textarea></div>`;
  },
  async add() { UI.openModal('Add Buyer', this.form(), async (body) => { const name=UI.val(body,'name'); if(!name){alert('Name required');return false;} await db.add('buyers',{name,phone:UI.val(body,'phone'),company:UI.val(body,'company'),email:UI.val(body,'email'),address:UI.val(body,'address'),buyerType:UI.val(body,'buyerType'),notes:UI.val(body,'notes'),totalPurchased:0,totalOwed:0}); this.render(); }); },
  async edit(id) { const b=await db.get('buyers',id); UI.openModal('Edit Buyer',this.form(b),async(body)=>{ await db.put('buyers',{...b,name:UI.val(body,'name'),phone:UI.val(body,'phone'),company:UI.val(body,'company'),email:UI.val(body,'email'),address:UI.val(body,'address'),buyerType:UI.val(body,'buyerType'),notes:UI.val(body,'notes')}); this.render(); }); },
  async del(id,name) { if(!UI.confirm(`Delete buyer "${name}"?`))return; await db.delete('buyers',id); this.render(); },
};
