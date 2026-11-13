import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CONFIG } from '../../../../core/config';
import { PageInfoComponent, SectionInfo } from '../page-info/page-info.component';

/** M13 : conditions d'utilisation (/infos/conditions). Texte à faire relire (dossier 19, V10). */
@Component({
  selector: 'db-conditions',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageInfoComponent],
  templateUrl: './conditions.component.html',
  styleUrl: './conditions.component.scss',
})
export class ConditionsComponent {
  protected readonly miseAJour = CONFIG.miseAJourTextes;
  protected readonly sections: SectionInfo[] = [
    {
      titre: 'Présentation du service',
      paragraphes: [
        'DevBrik est une plateforme communautaire de partage de briques de code réutilisables : chaque brique est publiée avec son code, un mode d’emploi, une licence permissive et des preuves de fonctionnement (notes, avis et tests datés).',
        'DevBrik stocke et affiche le code publié en texte brut. La plateforme n’exécute jamais le code des briques.',
      ],
    },
    {
      titre: 'Création du compte',
      paragraphes: [
        'La création d’un compte demande uniquement un pseudo et un mot de passe. Le nom affiché, le pays, la bio et le lien de profil sont facultatifs.',
        'Chaque utilisateur est responsable de la confidentialité de son mot de passe et des informations qu’il renseigne.',
      ],
    },
    {
      titre: 'Publication des briques',
      paragraphes: [
        'En publiant une brique, l’auteur garantit qu’il détient les droits nécessaires sur le code et les textes publiés.',
        'La brique reste soumise à la licence permissive choisie par l’auteur (MIT, Apache 2.0, BSD 2-Clause, BSD 3-Clause, ISC ou CC0 1.0) : toute personne peut la réutiliser dans les conditions de cette licence.',
        'L’auteur autorise DevBrik à stocker, afficher et rendre téléchargeable ce contenu sur la plateforme. Le contenu publié reste utilisable selon sa licence, même si la plateforme change d’exploitant.',
      ],
    },
    {
      titre: 'Responsabilités',
      paragraphes: [
        'Les briques sont fournies par la communauté, sans garantie. Le signal « testé » et les notes reflètent l’expérience déclarée des utilisateurs pour une version donnée du code ; ils ne remplacent pas vos propres vérifications.',
        'Chaque utilisateur est responsable du contenu qu’il publie (briques, avis, tests, demandes, réponses) et de l’usage qu’il fait des briques d’autrui.',
      ],
    },
    {
      titre: 'Modération',
      paragraphes: [
        'Tout utilisateur connecté peut signaler une brique (contenu inapproprié, code dangereux, violation de droits d’auteur, informations incorrectes, spam ou autre).',
        'Les administrateurs examinent les signalements et peuvent masquer, republier ou supprimer une brique, ainsi que supprimer un avis, une demande ou une réponse.',
      ],
    },
    {
      titre: 'Contact',
      paragraphes: [`Pour toute question sur ces conditions : ${CONFIG.contact}.`],
    },
  ];
}
