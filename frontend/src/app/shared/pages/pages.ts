import { Component } from '@angular/core';
import { EtatPage } from '../etat-page/etat-page';

@Component({
  selector: 'app-page-introuvable',
  imports: [EtatPage],
  template: `<app-etat-page mode="introuvable" actionLibelle="Retour aux briques" actionLien="/briques" />`,
})
export class PageIntrouvable {}

@Component({
  selector: 'app-acces-refuse',
  imports: [EtatPage],
  template: `<app-etat-page mode="acces-refuse" actionLibelle="Retour aux briques" actionLien="/briques" />`,
})
export class AccesRefuse {}

/** Page provisoire : le propriétaire du module la remplace par son écran. */
@Component({
  selector: 'app-page-en-construction',
  imports: [EtatPage],
  template: `
    <app-etat-page
      mode="vide"
      titre="Page en construction"
      message="Cet écran sera livré par son propriétaire (dossier de référence, section 12)." />
  `,
})
export class PageEnConstruction {}