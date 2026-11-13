import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CONFIG } from '../../../../core/config';
import { PageInfoComponent, SectionInfo } from '../page-info/page-info.component';

/** M13 : politique de confidentialité (/infos/confidentialite). Texte à faire relire (dossier 19, V10). */
@Component({
  selector: 'db-confidentialite',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageInfoComponent],
  templateUrl: './confidentialite.component.html',
  styleUrl: './confidentialite.component.scss',
})
export class ConfidentialiteComponent {
  protected readonly miseAJour = CONFIG.miseAJourTextes;
  protected readonly sections: SectionInfo[] = [
    {
      titre: 'Données collectées',
      paragraphes: ['DevBrik collecte uniquement les données nécessaires au service :'],
      liste: [
        'le pseudo (obligatoire) ;',
        'le mot de passe, conservé uniquement sous forme hachée (BCrypt) et jamais affiché ni journalisé ;',
        'le nom affiché, le pays, la bio et le lien de profil, s’ils sont renseignés (facultatifs) ;',
        'les contenus publiés : briques, avis, tests, demandes, réponses et signalements, avec leurs dates.',
      ],
    },
    {
      titre: 'Utilisation des données',
      paragraphes: [
        'Les données servent à gérer les comptes, afficher les contenus publiés, calculer la réputation et modérer la plateforme. Elles ne sont ni vendues ni transmises à des tiers, et aucun service externe (publicité, analyse, réseaux sociaux) n’est utilisé.',
        'Aucune adresse e-mail n’est demandée et aucun e-mail n’est envoyé.',
      ],
    },
    {
      titre: 'Conservation',
      paragraphes: [
        'Les données sont conservées tant que le compte existe. Les sauvegardes de la base de données sont conservées 7 jours au plus, puis remplacées.',
        'Le jeton de connexion est conservé dans votre navigateur et expire au bout de 12 heures.',
      ],
    },
    {
      titre: 'Hébergement et sécurité',
      paragraphes: [
        'DevBrik est hébergé sur un serveur cloud Datacloud situé à Madrid (Espagne).',
        'Les échanges sont chiffrés (HTTPS), les mots de passe sont hachés, la base de données n’est pas exposée sur Internet et l’accès au serveur est limité.',
      ],
    },
    {
      titre: 'Droits des utilisateurs',
      paragraphes: [
        'Depuis « Mon compte », vous pouvez consulter et modifier votre profil, et exporter à tout moment une copie de vos données au format JSON.',
      ],
    },
    {
      titre: 'Suppression du compte',
      paragraphes: [
        'Vous pouvez supprimer votre compte depuis « Mon compte », après confirmation par mot de passe. La suppression efface aussi vos briques, avis, tests, demandes, réponses et signalements. Elle est définitive.',
      ],
    },
    {
      titre: 'Contact',
      paragraphes: [`Pour toute question sur vos données : ${CONFIG.contact}.`],
    },
  ];
}
