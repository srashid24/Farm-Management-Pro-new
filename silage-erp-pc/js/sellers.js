const Sellers = {
  TYPES: ['Seeds','Fertilizer','Wrap','Machinery','Fuel','Services','Feed','Other'],
  async render() {
    UI.title('Suppliers / Sellers');
    const rows = await db.getAll('sellers');
    const tableRows = rows.length ? rows.map(s=>`<tr><td><strong>${s.name}</strong></td><td>${s.phone||''}</td><td>${s.company||''}</td><td>${s.address||''}</td><td>${s.supplyType||''}</td><td>${s.email||''}</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="Sellers.edit(${s.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Sellers.del(${s.id},'${s.name.replace(/'/g,"\\'")}')">Delete</button></td></tr>`).join('') : `<tr><td colspan="7">${UI.empty()}</td></tr>`;
    UI.render(`
      <div class="toolbar"><input type="text" placeholder="Search suppliers...">
        <button class="btn btn-primary" onclick="Sellers.add()">+ Add Supplier</button></div>
      <div class="card"><div class="table-wrap"><table id="tbl">
        <thead><tr><th>Name</th><th>Phone</th><th>Company</th><th>Address</th><th>Supply Type</th><th>Email</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },
  form(s={}) {
    return `<div class="form-row"><div class="form-group"><label>Full Name *</label><input id="name" value="${s.name||''}"></div>
      <div class="form-group"><label>Phone</label><input id="phone" value="${s.phone||''}"></div></div>
      <div class="form-row"><div class="form-group"><label>Company</label><input id="company" value="${s.company||''}"></div>
      <div class="form-group"><label>Email</label><input id="email" type="email" value="${s.email||''}"></div></div>
      <div class="form-group"><label>Address</label><input id="address" value="${s.address||''}"></div>
      <div class="form-group"><label>Supply Type</label><select id="supplyType">${UI.options(this.TYPES,s.supplyType)}</select></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${s.notes||''}</textarea></div>`;
  },
  async add() { UI.openModal('Add Supplier',this.form(),async(body)=>{ const name=UI.val(body,'name'); if(!name){alert('Name required');return false;} await db.add('sellers',{name,phone:UI.val(body,'phone'),company:UI.val(body,'company'),email:UI.val(body,'email'),address:UI.val(body,'address'),supplyType:UI.val(body,'supplyType'),notes:UI.val(body,'notes')}); this.render(); }); },
  async edit(id) { const s=await db.get('sellers',id); UI.openModal('Edit Supplier',this.form(s),async(body)=>{ await db.put('sellers',{...s,name:UI.val(body,'name'),phone:UI.val(body,'phone'),company:UI.val(body,'company'),email:UI.val(body,'email'),address:UI.val(body,'address'),supplyType:UI.val(body,'supplyType'),notes:UI.val(body,'notes')}); this.render(); }); },
  async del(id,name) { if(!UI.confirm(`Delete supplier "${name}"?`))return; await db.delete('sellers',id); this.render(); },
};
