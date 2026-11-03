export interface ReferenceUtilisateur {
  id: number;
  pseudo: string;
  nomAffiche: string | null;
}

export interface Utilisateur {
  id: number;
  pseudo: string;
  nomAffiche: string | null;
  role: 'DEVELOPPEUR' | 'ADMIN';
  pays: string | null;
  bio: string | null;
  lienProfil: string | null;
  dateCreation: string;
}

export interface PageReponse<T> {
  contenu: T[];
  page: number;
  taille: number;
  totalElements: number;
  totalPages: number;
}

export interface DetailErreur {
  champ: string;
  message: string;
}

export interface ErreurApi {
  statut: number;
  code: string;
  message: string;
  details?: DetailErreur[];
  horodatage: string;
}

export interface ReponseAuth {
  jeton: string;
  utilisateur: Utilisateur;
}