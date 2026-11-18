import { Component, inject } from '@angular/core';

import { NotificationService } from '../../services/notification.service';

@Component({
  // Balise a placer dans app.html : <app-notification />
  selector: 'app-notification',
  imports: [],
  templateUrl: './notification.html',
  styleUrl: './notification.css',
})
export class Notification {
  // Le service partage : c'est lui qui detient le message courant.
  // "protected" : le template (le fichier HTML) peut y acceder.
  protected service = inject(NotificationService);
}
