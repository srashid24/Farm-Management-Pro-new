/** Lands / Fields Module */
const Lands = {
  SOIL_TYPES: ['Clay','Sandy','Loam','Clay Loam','Sandy Loam','Silt','Other'],
  CROP_TYPES: ['Maize','Grass','Sorghum','Oats','Barley','Mixed','Other'],
  CONTRACT_TYPES: ['Owned','Leased','Rented','Communal','Other'],

  async render() {
    UI.title('Lands & Fields');
    const [rows, farmers] = await Promise.all([db.getAll('lands'), db.getAll('farmers')]);
    const fMap = Object.fromEntries(farmers.map(f => [f.id, f.name]));
    const totalHa = rows.reduce((s,r) => s + (r.sizeHectares||0), 0);
    const totalAc = rows.reduce((s,r) => s + (r.sizeAcres||0), 0);

    const tableRows = rows.length ? rows.map(l => `
      <tr>
        <td>${l.blockId||''}</td>
        <td><strong>${l.fieldName}</strong></td>
        <td>${fMap[l.farmerId]||''}</td>
        <td>${UI.n(l.sizeAcres,1)} ac / ${UI.n(l.sizeHectares,2)} ha</td>
        <td>${l.location||''}</td>
        <td>${l.cropType||''}</td>
        <td>${l.soilType||''}</td>
        <td>${l.contractType||''}</td>
        <td>${l.landownerName||''}</td>
        <td>${l.contractCost ? UI.money(l.contractCost)+'/season' : ''}</td>
        <td class="actions">
          <button class="btn btn-sm btn-outline" onclick="Lands.edit(${l.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Lands.del(${l.id},'${l.fieldName.replace(/'/g,"\\'")}')">Delete</button>
        </td>
      </tr>`).join('') : `<tr><td colspan="11">${UI.empty()}</td></tr>`;

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Fields</div><div class="kpi-value">${rows.length}</div></div>
        <div class="kpi green"><div class="kpi-label">Total Acres</div><div class="kpi-value">${UI.n(totalAc,1)}</div></div>
        <div class="kpi green"><div class="kpi-label">Total Hectares</div><div class="kpi-value">${UI.n(totalHa,2)}</div></div>
      </div>
      <div class="toolbar">
        <input type="text" placeholder="Search lands..." oninput="Lands.search(this.value)">
        <button class="btn btn-primary" onclick="Lands.add()">+ Add Land</button>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table id="tbl">
            <thead><tr><th>Block ID</th><th>Field Name</th><th>Farmer</th><th>Size</th><th>Location</th><th>Crop</th><th>Soil</th><th>Contract</th><th>Landowner</th><th>Rent/Season</th><th>Actions</th></tr></thead>
            <tbody>${tableRows}</tbody>
          </table>
        </div>
      </div>`);
  },

  async search(q) {
    const rows = await db.getAll('lands');
    const filtered = q ? rows.filter(l => (l.fieldName+l.location+l.blockId).toLowerCase().includes(q.toLowerCase())) : rows;
    const farmers = await db.getAll('farmers');
    const fMap = Object.fromEntries(farmers.map(f => [f.id, f.name]));
    document.querySelector('#tbl tbody').innerHTML = filtered.map(l => `<tr><td>${l.blockId||''}</td><td>${l.fieldName}</td><td>${fMap[l.farmerId]||''}</td><td>${UI.n(l.sizeAcres,1)} ac</td><td>${l.location||''}</td><td>${l.cropType||''}</td><td>${l.soilType||''}</td><td>${l.contractType||''}</td><td>${l.landownerName||''}</td><td>${l.contractCost?UI.money(l.contractCost):''}</td><td class="actions"><button class="btn btn-sm btn-outline" onclick="Lands.edit(${l.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Lands.del(${l.id},'${l.fieldName.replace(/'/g,"\\'")}')">Delete</button></td></tr>`).join('') || `<tr><td colspan="11" style="text-align:center;color:#999">No results</td></tr>`;
  },

  async form(l = {}) {
    const farmers = await db.getAll('farmers');
    return `
      <div class="form-row">
        <div class="form-group"><label>Farmer / Owner</label>
          <select id="farmerId"><option value="">— Select Farmer —</option>${UI.objOptions(farmers,'id','name',l.farmerId)}</select></div>
        <div class="form-group"><label>Block / Field ID</label><input id="blockId" value="${l.blockId||''}"></div>
      </div>
      <div class="form-group"><label>Field Name *</label><input id="fieldName" value="${l.fieldName||''}"></div>
      <div class="form-row">
        <div class="form-group"><label>Size in Acres</label><input id="sizeAcres" type="number" step="0.1" value="${l.sizeAcres||''}" oninput="Lands.calcHa(this.value)"></div>
        <div class="form-group"><label>Hectares (auto)</label><input id="sizeHectares" readonly style="background:#f5f5f5" value="${UI.n(l.sizeHectares,3)||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Location / Village</label><input id="location" value="${l.location||''}"></div>
        <div class="form-group"><label>GPS Coordinates</label><input id="gpsCoordinates" value="${l.gpsCoordinates||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Soil Type</label><select id="soilType">${UI.options(this.SOIL_TYPES, l.soilType)}</select></div>
        <div class="form-group"><label>Crop / Silage Type</label><select id="cropType">${UI.options(this.CROP_TYPES, l.cropType)}</select></div>
      </div>
      <div class="form-section">Contract / Ownership Details</div>
      <div class="form-row">
        <div class="form-group"><label>Contract Type</label><select id="contractType">${UI.options(this.CONTRACT_TYPES, l.contractType)}</select></div>
        <div class="form-group"><label>Landowner Name</label><input id="landownerName" value="${l.landownerName||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Rent Cost / Season ($)</label><input id="contractCost" type="number" step="0.01" value="${l.contractCost||''}"></div>
        <div class="form-group"><label>Contract Start Date</label><input id="contractStartDate" type="date" value="${l.contractStartDate||''}"></div>
      </div>
      <div class="form-group"><label>Contract End Date</label><input id="contractEndDate" type="date" value="${l.contractEndDate||''}"></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${l.notes||''}</textarea></div>`;
  },

  calcHa(acres) {
    const ha = (parseFloat(acres)||0) / 2.47105;
    const el = document.getElementById('sizeHectares');
    if (el) el.value = ha.toFixed(3);
  },

  async add() {
    const formHtml = await this.form();
    UI.openModal('Add Land / Field', formHtml, async (body) => {
      const fieldName = UI.val(body,'fieldName');
      if (!fieldName) { alert('Field name is required'); return false; }
      const ac = UI.num(body,'sizeAcres');
      await db.add('lands', {
        farmerId: parseInt(UI.val(body,'farmerId'))||0,
        blockId: UI.val(body,'blockId'), fieldName, sizeAcres: ac,
        sizeHectares: ac / 2.47105,
        location: UI.val(body,'location'), gpsCoordinates: UI.val(body,'gpsCoordinates'),
        soilType: UI.val(body,'soilType'), cropType: UI.val(body,'cropType'),
        contractType: UI.val(body,'contractType'), landownerName: UI.val(body,'landownerName'),
        contractCost: UI.num(body,'contractCost'),
        contractStartDate: UI.val(body,'contractStartDate'),
        contractEndDate: UI.val(body,'contractEndDate'),
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async edit(id) {
    const l = await db.get('lands', id);
    const formHtml = await this.form(l);
    UI.openModal('Edit Land / Field', formHtml, async (body) => {
      const fieldName = UI.val(body,'fieldName');
      if (!fieldName) { alert('Field name is required'); return false; }
      const ac = UI.num(body,'sizeAcres');
      await db.put('lands', { ...l, farmerId: parseInt(UI.val(body,'farmerId'))||0,
        blockId: UI.val(body,'blockId'), fieldName, sizeAcres: ac,
        sizeHectares: ac / 2.47105,
        location: UI.val(body,'location'), gpsCoordinates: UI.val(body,'gpsCoordinates'),
        soilType: UI.val(body,'soilType'), cropType: UI.val(body,'cropType'),
        contractType: UI.val(body,'contractType'), landownerName: UI.val(body,'landownerName'),
        contractCost: UI.num(body,'contractCost'),
        contractStartDate: UI.val(body,'contractStartDate'),
        contractEndDate: UI.val(body,'contractEndDate'),
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async del(id, name) {
    if (!UI.confirm(`Delete field "${name}"?`)) return;
    await db.delete('lands', id);
    this.render();
  },
};
