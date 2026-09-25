const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const error = new Error(data.message || 'Erreur');
    error.code = data.code;
    error.status = response.status;
    error.body = data;
    throw error;
  }

  return data;
}

export const api = {
  // Sessions
  createSession: (titre, promotionId) => request('/sessions', {
    method: 'POST',
    body: JSON.stringify({ titre, promotionId }),
  }),

  closeSession: (id) => request(`/sessions/${id}/cloture`, { method: 'POST' }),

  // Presences
  markPresence: (code, etudiantId) => request('/presences', {
    method: 'POST',
    body: JSON.stringify({ code, etudiantId }),
  }),

  addPresenceByFormateur: (sessionId, etudiantId) => request(`/sessions/${sessionId}/presences`, {
    method: 'POST',
    body: JSON.stringify({ etudiantId }),
  }),

  // Exercices
  depositExercice: (sessionId, etudiantId, lien) => request('/exercices', {
    method: 'POST',
    body: JSON.stringify({ sessionId, etudiantId, lien }),
  }),

  replaceExerciceLien: (id, etudiantId, lien) => request(`/exercices/${id}/lien`, {
    method: 'PUT',
    body: JSON.stringify({ lien }),
  }),

  // Relectures
  renderRelecture: (id, relecteurId, note, commentaire) => request(`/relectures/${id}`, {
    method: 'POST',
    body: JSON.stringify({ note, commentaire }),
  }),

  correctRelecture: (id, relecteurId, note, commentaire) => request(`/relectures/${id}/correction`, {
    method: 'POST',
    body: JSON.stringify({ note, commentaire }),
  }),

  getRelecturesEnAttente: (relecteurId) => request(`/relectures/en-attente?relecteurId=${relecteurId}`),

  // Tableau
  getTableau: (promotionId) => request(`/tableau?promotionId=${promotionId}`),

  // Promotions / Etudiants
  getEtudiants: (promotionId) => request(`/promotions/${promotionId}/etudiants`),

  // Etudiant results
  getResultats: (etudiantId) => request(`/etudiants/${etudiantId}/resultats`),
};