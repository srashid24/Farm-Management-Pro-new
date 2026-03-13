/**
 * Silage Pro ERP - Offline IndexedDB Database Layer
 * No external dependencies - works completely offline
 */

const DB_NAME = 'SilageProERP';
const DB_VERSION = 1;

const STORES = {
  farmers:    { keyPath: 'id', autoIncrement: true },
  lands:      { keyPath: 'id', autoIncrement: true },
  harvests:   { keyPath: 'id', autoIncrement: true },
  inventory:  { keyPath: 'id', autoIncrement: true },
  buyers:     { keyPath: 'id', autoIncrement: true },
  sellers:    { keyPath: 'id', autoIncrement: true },
  sales:      { keyPath: 'id', autoIncrement: true },
  costs:      { keyPath: 'id', autoIncrement: true },
  cattle:     { keyPath: 'id', autoIncrement: true },
  fattening:  { keyPath: 'id', autoIncrement: true },
  packaging:  { keyPath: 'id', autoIncrement: true },
  purchases:  { keyPath: 'id', autoIncrement: true },
};

class DB {
  constructor() { this.db = null; }

  open() {
    return new Promise((resolve, reject) => {
      const req = indexedDB.open(DB_NAME, DB_VERSION);
      req.onupgradeneeded = e => {
        const db = e.target.result;
        Object.entries(STORES).forEach(([name, opts]) => {
          if (!db.objectStoreNames.contains(name)) {
            db.createObjectStore(name, opts);
          }
        });
      };
      req.onsuccess = e => { this.db = e.target.result; resolve(this); };
      req.onerror   = e => reject(e.target.error);
    });
  }

  _tx(store, mode = 'readonly') {
    return this.db.transaction(store, mode).objectStore(store);
  }

  getAll(store) {
    return new Promise((resolve, reject) => {
      const req = this._tx(store).getAll();
      req.onsuccess = e => resolve(e.target.result);
      req.onerror   = e => reject(e.target.error);
    });
  }

  get(store, id) {
    return new Promise((resolve, reject) => {
      const req = this._tx(store).get(Number(id));
      req.onsuccess = e => resolve(e.target.result);
      req.onerror   = e => reject(e.target.error);
    });
  }

  add(store, record) {
    record.createdAt = Date.now();
    return new Promise((resolve, reject) => {
      const req = this._tx(store, 'readwrite').add(record);
      req.onsuccess = e => resolve(e.target.result);
      req.onerror   = e => reject(e.target.error);
    });
  }

  put(store, record) {
    return new Promise((resolve, reject) => {
      const req = this._tx(store, 'readwrite').put(record);
      req.onsuccess = e => resolve(e.target.result);
      req.onerror   = e => reject(e.target.error);
    });
  }

  delete(store, id) {
    return new Promise((resolve, reject) => {
      const req = this._tx(store, 'readwrite').delete(Number(id));
      req.onsuccess = e => resolve();
      req.onerror   = e => reject(e.target.error);
    });
  }

  async count(store) {
    const all = await this.getAll(store);
    return all.length;
  }

  async sum(store, field) {
    const all = await this.getAll(store);
    return all.reduce((s, r) => s + (Number(r[field]) || 0), 0);
  }
}

const db = new DB();
