import { computed, signal } from '@angular/core';
import { Avis } from './avis.modeles';

export class SocleFactice {
  readonly idUtilisateur = signal<number | null>(10);
  readonly connecte = computed(() => this.idUtilisateur() !== null);
  readonly estAdmin = signal(false);
  readonly messagesSucces: string[] = [];
  readonly messagesErreur: string[] = [];
  connexions = 0;

  succes(message: string): void {
    this.messagesSucces.push(message);
  }

  erreur(message: string): void {
    this.messagesErreur.push(message);
  }

  versConnexion(): void {
    this.connexions++;
  }
}

export const AVIS_EXEMPLE: Avis = {
  id: 1,
  note: 4,
  commentaire: null,
  auteur: { id: 10, pseudo: 'awa.dev', nomAffiche: 'Awa Diop' },
  dateCreation: '2026-11-05T14:30:00Z',
  dateModification: '2026-11-05T14:30:00Z',
};

export const attendre = (): Promise<void> => new Promise((resolve) => setTimeout(resolve, 0));

export function requis<T>(valeur: T | null | undefined): T {
  if (valeur === null || valeur === undefined) {
    throw new Error('Élément attendu introuvable');
  }
  return valeur;
}

export function saisir(racine: HTMLElement, selecteur: string, texte: string): void {
  const champ = requis(racine.querySelector<HTMLInputElement | HTMLTextAreaElement>(selecteur));
  champ.value = texte;
  champ.dispatchEvent(new Event('input'));
}
