import { useState, useEffect } from 'react';
import { api } from '../services/api';

export function useEtudiants(promotionId) {
  const [etudiants, setEtudiants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!promotionId) return;
    setLoading(true);
    api.getEtudiants(promotionId)
      .then(setEtudiants)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [promotionId]);

  return { etudiants, loading, error };
}