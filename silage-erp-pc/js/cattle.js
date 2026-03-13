const CattleModule = {
  BREEDS: ['Brahman','Nguni','Bonsmara','Simmentaler','Angus','Holstein','Hereford','Afrikaner','Mixed','Other'],
  GENDERS: ['Bull','Cow','Heifer','Steer','Calf (M)','Calf (F)'],
  STATUSES: ['Active','Fattening','Sold','Deceased'],
  SOURCES: ['Born on Farm','Purchased','Auction','Other'],
  async render() {
    UI.title('Cattle');
    const [rows, farmers] = await Promise.all([db.getAll('cattle'), db.getAll('farmers')]);
    const fMap = Object.fromEntries(farmers.map(f=>[f.id,f.name]));
    const active = rows.filter(r=>r.status==='Active').length;
    const tableRows = rows.length ? rows.map(c=>`<tr>
      <td><strong>${c.tagNumber}</strong></td><td>${c.name||''}</td><td>${fMap[c.ownerId]||''}</td>
      <td>${c.breed||''}</td><td>${c.gender||''}</td><td>${UI.n(c.currentWeightKg,1)} kg</td>
      <td>${c.color||''}</td><td>${c.dateOfBirth||''}</td>
      <td>${UI.badge(c.status, c.status==='Active'?'green':c.status==='Fattening'?'blue':'grey')}</td>
      ${c.imagePath?`<td><img src="${c.imagePath}" class="photo-thumb"></td>`:'<td>—</td>'}
      <td class="actions"><button class="btn btn-sm btn-outline" onclick="CattleModule.edit(${c.id})">Edit</button>
      <button class="btn btn-sm btn-danger" onclick="CattleModule.del(${c.id},'${c.tagNumber}')">Delete</button></td></tr>`).join('') : `<tr><td colspan="11">${UI.empty()}</td></tr>`;
    UI.render(`
      <div class="kpi-grid">
        <div class="kpi blue"><div class="kpi-label">Total Cattle</div><div class="kpi-value">${rows.length}</div></div>
        <div class="kpi green"><div class="kpi-label">Active</div><div class="kpi-value">${active}</div></div>
      </div>
      <div class="toolbar"><input type="text" placeholder="Search cattle..." oninput="CattleModule.search(this.value)">
        <button class="btn btn-primary" onclick="CattleModule.add()">+ Add Cattle</button></div>
      <div class="card"><div class="table-wrap"><table id="tbl">
        <thead><tr><th>Tag</th><th>Name</th><th>Owner</th><th>Breed</th><th>Gender</th><th>Weight</th><th>Colour</th><th>DOB</th><th>Status</th><th>Photo</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },
  async search(q) { const [rows,farmers]=await Promise.all([db.getAll('cattle'),db.getAll('farmers')]); const fMap=Object.fromEntries(farmers.map(f=>[f.id,f.name])); const f=q?rows.filter(c=>(c.tagNumber+c.name+c.breed).toLowerCase().includes(q.toLowerCase())):rows; document.querySelector('#tbl tbody').innerHTML=f.map(c=>`<tr><td>${c.tagNumber}</td><td>${c.name||''}</td><td>${fMap[c.ownerId]||''}</td><td>${c.breed}</td><td>${c.gender}</td><td>${UI.n(c.currentWeightKg,1)} kg</td><td>${c.color||''}</td><td>${c.dateOfBirth||''}</td><td>${c.status}</td><td>—</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="CattleModule.edit(${c.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="CattleModule.del(${c.id},'${c.tagNumber}')">Del</button></td></tr>`).join('')||'<tr><td colspan="11" style="text-align:center;color:#999">No results</td></tr>'; },
  async form(c={}) { const farmers=await db.getAll('farmers'); return `
    <div class="form-row"><div class="form-group"><label>Owner / Farmer</label><select id="ownerId"><option value="">— Select —</option>${UI.objOptions(farmers,'id','name',c.ownerId)}</select></div>
      <div class="form-group"><label>Tag Number *</label><input id="tagNumber" value="${c.tagNumber||''}"></div></div>
    <div class="form-row"><div class="form-group"><label>Name (optional)</label><input id="name" value="${c.name||''}"></div>
      <div class="form-group"><label>Colour / Markings</label><input id="color" value="${c.color||''}"></div></div>
    <div class="form-row"><div class="form-group"><label>Breed</label><select id="breed">${UI.options(this.BREEDS,c.breed)}</select></div>
      <div class="form-group"><label>Gender</label><select id="gender">${UI.options(this.GENDERS,c.gender)}</select></div></div>
    <div class="form-row"><div class="form-group"><label>Date of Birth</label><input id="dateOfBirth" type="date" value="${c.dateOfBirth||''}"></div>
      <div class="form-group"><label>Current Weight (kg)</label><input id="currentWeightKg" type="number" step="0.1" value="${c.currentWeightKg||''}"></div></div>
    <div class="form-row"><div class="form-group"><label>Status</label><select id="status">${UI.options(this.STATUSES,c.status||'Active')}</select></div>
      <div class="form-group"><label>Source</label><select id="source">${UI.options(this.SOURCES,c.source)}</select></div></div>
    <div class="form-row"><div class="form-group"><label>Purchase Price ($)</label><input id="purchasePrice" type="number" step="0.01" value="${c.purchasePrice||''}"></div>
      <div class="form-group"><label>Purchase Date</label><input id="purchaseDate" type="date" value="${c.purchaseDate||''}"></div></div>
    <div class="form-group"><label>Notes</label><textarea id="notes">${c.notes||''}</textarea></div>
    ${UI.photoField('photo',c.imagePath)}`; },
  async add() { const f=await this.form(); UI.openModal('Add Cattle',f,async(body)=>{ const tag=UI.val(body,'tagNumber'); if(!tag){alert('Tag required');return false;} const photo=await UI.readPhoto(body.querySelector('#photo')); await db.add('cattle',{ownerId:parseInt(UI.val(body,'ownerId'))||0,tagNumber:tag,name:UI.val(body,'name'),breed:UI.val(body,'breed'),gender:UI.val(body,'gender'),dateOfBirth:UI.val(body,'dateOfBirth'),currentWeightKg:UI.num(body,'currentWeightKg'),status:UI.val(body,'status'),color:UI.val(body,'color'),source:UI.val(body,'source'),purchasePrice:UI.num(body,'purchasePrice'),purchaseDate:UI.val(body,'purchaseDate'),notes:UI.val(body,'notes'),imagePath:photo}); this.render(); }); },
  async edit(id) { const c=await db.get('cattle',id); const f=await this.form(c); UI.openModal('Edit Cattle',f,async(body)=>{ const photo=await UI.readPhoto(body.querySelector('#photo')); await db.put('cattle',{...c,ownerId:parseInt(UI.val(body,'ownerId'))||0,tagNumber:UI.val(body,'tagNumber'),name:UI.val(body,'name'),breed:UI.val(body,'breed'),gender:UI.val(body,'gender'),dateOfBirth:UI.val(body,'dateOfBirth'),currentWeightKg:UI.num(body,'currentWeightKg'),status:UI.val(body,'status'),color:UI.val(body,'color'),source:UI.val(body,'source'),purchasePrice:UI.num(body,'purchasePrice'),purchaseDate:UI.val(body,'purchaseDate'),notes:UI.val(body,'notes'),imagePath:photo||c.imagePath}); this.render(); }); },
  async del(id,tag) { if(!UI.confirm(`Delete cattle "${tag}"?`))return; await db.delete('cattle',id); this.render(); },
};
