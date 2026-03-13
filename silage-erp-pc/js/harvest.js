/** Harvest / Yield Module */
const Harvest = {
  QUALITY: ['Excellent','Good','Fair','Poor'],
  TYPES: ['Maize','Grass','Sorghum','Oats','Mixed','Other'],

  async render() {
    UI.title('Harvest & Yield');
    const [rows, lands] = await Promise.all([db.getAll('harvests'), db.getAll('lands')]);
    const lMap = Object.fromEntries(lands.map(l => [l.id, l.fieldName]));
    const totalTons = rows.reduce((s,r) => s+(r.yieldTons||0), 0);
    const totalBales = rows.reduce((s,r) => s+(r.baleCount||0), 0);

    const tableRows = rows.length ? rows.map(h => `
      <tr>
        <td>${h.harvestDate||''}</td>
        <td>${h.season||''}</td>
        <td>${lMap[h.landId]||''}</td>
        <td>${h.silagType||''}</td>
        <td class="text-green fw-bold">${UI.n(h.yieldTons,1)} t</td>
        <td>${h.baleCount||0} bales</td>
        <td>${UI.n(h.moisturePercent,1)}%</td>
        <td>${h.quality ? UI.badge(h.quality, h.quality==='Excellent'||h.quality==='Good'?'green':'orange') : ''}</td>
        <td>${h.storageLocation||''}</td>
        ${h.imagePath ? `<td><img src="${h.imagePath}" class="photo-thumb"></td>` : '<td>—</td>'}
        <td class="actions">
          <button class="btn btn-sm btn-outline" onclick="Harvest.edit(${h.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Harvest.del(${h.id})">Delete</button>
        </td>
      </tr>`).join('') : `<tr><td colspan="11">${UI.empty()}</td></tr>`;

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi brown"><div class="kpi-label">Total Yield</div><div class="kpi-value">${UI.n(totalTons,1)} t</div></div>
        <div class="kpi brown"><div class="kpi-label">Total Bales</div><div class="kpi-value">${totalBales}</div></div>
        <div class="kpi brown"><div class="kpi-label">Harvests</div><div class="kpi-value">${rows.length}</div></div>
      </div>
      <div class="toolbar">
        <button class="btn btn-primary" onclick="Harvest.add()">+ Add Harvest Record</button>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table id="tbl">
            <thead><tr><th>Date</th><th>Season</th><th>Field</th><th>Type</th><th>Yield</th><th>Bales</th><th>Moisture</th><th>Quality</th><th>Storage</th><th>Photo</th><th>Actions</th></tr></thead>
            <tbody>${tableRows}</tbody>
          </table>
        </div>
      </div>`);
  },

  async form(h = {}) {
    const lands = await db.getAll('lands');
    return `
      <div class="form-row">
        <div class="form-group"><label>Field *</label>
          <select id="landId"><option value="">— Select Field —</option>${UI.objOptions(lands,'id','fieldName',h.landId)}</select></div>
        <div class="form-group"><label>Season</label><input id="season" value="${h.season||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Harvest Date</label><input id="harvestDate" type="date" value="${h.harvestDate||UI.today()}"></div>
        <div class="form-group"><label>Silage Type</label><select id="silagType">${UI.options(this.TYPES, h.silagType)}</select></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Yield (Tons)</label><input id="yieldTons" type="number" step="0.1" value="${h.yieldTons||''}"></div>
        <div class="form-group"><label>Moisture %</label><input id="moisturePercent" type="number" step="0.1" value="${h.moisturePercent||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Number of Bales</label><input id="baleCount" type="number" value="${h.baleCount||''}"></div>
        <div class="form-group"><label>Bale Weight (kg)</label><input id="baleWeightKg" type="number" step="0.1" value="${h.baleWeightKg||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Quality</label><select id="quality">${UI.options(this.QUALITY, h.quality)}</select></div>
        <div class="form-group"><label>Storage Location</label><input id="storageLocation" value="${h.storageLocation||''}"></div>
      </div>
      <div class="form-group"><label>Total Input Cost ($)</label><input id="inputCostTotal" type="number" step="0.01" value="${h.inputCostTotal||''}"></div>
      <div class="form-group"><label>Notes / Sample Quality Description</label><textarea id="notes">${h.notes||''}</textarea></div>
      ${UI.photoField('photo', h.imagePath)}`;
  },

  async add() {
    const formHtml = await this.form();
    UI.openModal('Add Harvest Record', formHtml, async (body) => {
      const landId = parseInt(UI.val(body,'landId'));
      if (!landId) { alert('Select a field'); return false; }
      const photo = await UI.readPhoto(body.querySelector('#photo'));
      await db.add('harvests', {
        landId, season: UI.val(body,'season'), harvestDate: UI.val(body,'harvestDate'),
        yieldTons: UI.num(body,'yieldTons'), moisturePercent: UI.num(body,'moisturePercent'),
        baleCount: parseInt(UI.val(body,'baleCount'))||0, baleWeightKg: UI.num(body,'baleWeightKg'),
        quality: UI.val(body,'quality'), silagType: UI.val(body,'silagType'),
        storageLocation: UI.val(body,'storageLocation'),
        inputCostTotal: UI.num(body,'inputCostTotal'),
        notes: UI.val(body,'notes'), imagePath: photo || h?.imagePath || null,
      });
      this.render();
    });
  },

  async edit(id) {
    const h = await db.get('harvests', id);
    const formHtml = await this.form(h);
    UI.openModal('Edit Harvest Record', formHtml, async (body) => {
      const landId = parseInt(UI.val(body,'landId'));
      if (!landId) { alert('Select a field'); return false; }
      const photo = await UI.readPhoto(body.querySelector('#photo'));
      await db.put('harvests', { ...h,
        landId, season: UI.val(body,'season'), harvestDate: UI.val(body,'harvestDate'),
        yieldTons: UI.num(body,'yieldTons'), moisturePercent: UI.num(body,'moisturePercent'),
        baleCount: parseInt(UI.val(body,'baleCount'))||0, baleWeightKg: UI.num(body,'baleWeightKg'),
        quality: UI.val(body,'quality'), silagType: UI.val(body,'silagType'),
        storageLocation: UI.val(body,'storageLocation'),
        inputCostTotal: UI.num(body,'inputCostTotal'),
        notes: UI.val(body,'notes'), imagePath: photo || h.imagePath,
      });
      this.render();
    });
  },

  async del(id) {
    if (!UI.confirm('Delete this harvest record?')) return;
    await db.delete('harvests', id);
    this.render();
  },
};
